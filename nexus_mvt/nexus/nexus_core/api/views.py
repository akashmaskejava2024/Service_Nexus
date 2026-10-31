"""
views.py — DRF API Views for Service Nexus.

Sections
--------
A. Authentication  (/api/auth/)
B. Customer        (/api/requests/, /api/bids/)
C. Worker          (/api/worker/)
D. Jobs & Reviews  (/api/jobs/)
"""

import base64
from io import BytesIO

from django.contrib.auth import authenticate, get_user_model
from django.shortcuts import get_object_or_404
from rest_framework import status
from rest_framework.parsers import MultiPartParser, FormParser, JSONParser
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken

from nexus_core.models import WorkerProfile, ServiceRequest, Bid, Review

from .permissions import IsCustomer, IsWorker
from .serializers import (
    CustomerRegisterSerializer,
    WorkerRegisterSerializer,
    LoginSerializer,
    ServiceRequestCreateSerializer,
    ServiceRequestListSerializer,
    ServiceRequestDashboardSerializer,
    BidCreateSerializer,
    BidReadSerializer,
    JobCompleteSerializer,
    ReviewSerializer,
)
from .utils import haversine_distance, WORKER_RADIUS_KM

User = get_user_model()


# ---------------------------------------------------------------------------
# Helper
# ---------------------------------------------------------------------------

def _jwt_tokens_for(user) -> dict:
    """Return a dict with refresh + access JWT strings for *user*."""
    refresh = RefreshToken.for_user(user)
    return {
        "refresh": str(refresh),
        "access": str(refresh.access_token),
    }


# ===========================================================================
# A. Authentication Endpoints
# ===========================================================================

class RegisterCustomerView(APIView):
    """
    POST /api/auth/register/customer/

    Creates a new CUSTOMER account.
    Returns JWT tokens so the client can immediately authenticate.
    """

    authentication_classes = []
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = CustomerRegisterSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.save()
        tokens = _jwt_tokens_for(user)
        return Response(
            {
                **tokens,
                "user_id": user.id,
                "username": user.username,
                "user_type": user.user_type,
            },
            status=status.HTTP_201_CREATED,
        )


class RegisterWorkerView(APIView):
    """
    POST /api/auth/register/worker/

    Creates a new WORKER account and their WorkerProfile in one step.
    Requires: username, email, password, password2, category, latitude, longitude.
    Returns JWT tokens.
    """

    authentication_classes = []
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = WorkerRegisterSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.save()
        tokens = _jwt_tokens_for(user)
        return Response(
            {
                **tokens,
                "user_id": user.id,
                "username": user.username,
                "user_type": user.user_type,
            },
            status=status.HTTP_201_CREATED,
        )


class LoginView(APIView):
    """
    POST /api/auth/login/

    Mirrors the MVT project's AuthenticationForm — authenticates with
    username + password (NOT email) and returns JWT access/refresh tokens
    along with the user_type so the client can redirect accordingly.

    Request body:
        { "username": "alice", "password": "StrongPass123!" }

    Response (200):
        {
            "refresh": "...",
            "access":  "...",
            "user_id": 1,
            "username": "alice",
            "user_type": "CUSTOMER"   // or "WORKER"
        }
    """

    authentication_classes = []
    permission_classes = [AllowAny]

    def post(self, request):
        # Validate input through the LoginSerializer (username + password only)
        serializer = LoginSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        username = serializer.validated_data["username"]
        password = serializer.validated_data["password"]

        # Django's authenticate() checks username + hashed password — same as MVT
        user = authenticate(request, username=username, password=password)

        if user is None:
            return Response(
                {"detail": "Invalid username or password. Please try again."},
                status=status.HTTP_401_UNAUTHORIZED,
            )

        tokens = _jwt_tokens_for(user)
        return Response(
            {
                **tokens,
                "user_id": user.id,
                "username": user.username,
                "user_type": user.user_type,
            },
            status=status.HTTP_200_OK,
        )


# ===========================================================================
# B. Customer Endpoints
# ===========================================================================

class ServiceRequestCreateView(APIView):
    """
    POST /api/requests/

    Creates a new ServiceRequest for the authenticated Customer.
    Accepts multipart/form-data so an optional photo can be uploaded.
    The secret_token (UUIDv4) is auto-generated by the model.
    """

    permission_classes = [IsCustomer]
    parser_classes = [MultiPartParser, FormParser, JSONParser]

    def post(self, request):
        serializer = ServiceRequestCreateSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        service_request = serializer.save(customer=request.user)
        return Response(
            ServiceRequestListSerializer(service_request).data,
            status=status.HTTP_201_CREATED,
        )


class MyRequestsView(APIView):
    """
    GET /api/requests/my-requests/

    Returns all ServiceRequests belonging to the logged-in Customer,
    with their associated Bids nested inside each request object.
    Optimised with prefetch_related to avoid N+1 queries.
    """

    permission_classes = [IsCustomer]

    def get(self, request):
        requests_qs = (
            ServiceRequest.objects.filter(customer=request.user)
            .prefetch_related("bids__worker__user")
            .order_by("-created_at")
        )
        serializer = ServiceRequestListSerializer(requests_qs, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class AcceptBidView(APIView):
    """
    POST /api/bids/<bid_id>/accept/

    Customer accepts a specific Bid.
    - Sets Bid.is_accepted = True
    - Sets the parent ServiceRequest.status = 'ACCEPTED'

    Business rules enforced:
    - Only the Customer who owns the ServiceRequest can call this.
    - The ServiceRequest must still be PENDING.
    """

    permission_classes = [IsCustomer]

    def post(self, request, bid_id):
        # Scoped lookup: bid must belong to a request owned by this customer
        bid = get_object_or_404(
            Bid,
            id=bid_id,
            service_request__customer=request.user,
        )

        if bid.is_accepted:
            return Response(
                {"detail": "This bid has already been accepted."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        service_req = bid.service_request
        if service_req.status != "PENDING":
            return Response(
                {"detail": f"Cannot accept a bid on a request with status '{service_req.status}'."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        # Atomically update bid + request in a transaction-safe manner
        bid.is_accepted = True
        bid.save(update_fields=["is_accepted"])

        service_req.status = "ACCEPTED"
        service_req.save(update_fields=["status"])

        return Response(
            {
                "detail": f"Bid #{bid.id} accepted. Request #{service_req.id} is now ACCEPTED.",
                "bid": BidReadSerializer(bid).data,
            },
            status=status.HTTP_200_OK,
        )


# ===========================================================================
# C. Worker Proximity & Bidding Endpoints
# ===========================================================================

class WorkerDashboardView(APIView):
    """
    GET /api/worker/dashboard/

    Returns service requests relevant to the authenticated Worker:

    - PENDING requests within 5 km of the worker's location (sorted nearest first).
    - ACCEPTED / COMPLETED requests only if this worker's bid was accepted.

    The Haversine formula is used for distance calculation.
    Distance (km) is annotated onto each result object as `distance_km`.
    """

    permission_classes = [IsWorker]

    def get(self, request):
        try:
            worker_profile = request.user.worker_profile
        except WorkerProfile.DoesNotExist:
            return Response(
                {"detail": "Worker profile not found. Please contact support."},
                status=status.HTTP_404_NOT_FOUND,
            )

        if worker_profile.latitude is None or worker_profile.longitude is None:
            return Response(
                {"detail": "Worker location is not set. Please update your profile."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        w_lat = worker_profile.latitude
        w_lon = worker_profile.longitude

        all_requests = ServiceRequest.objects.prefetch_related(
            "bids__worker"
        ).filter(status__in=["PENDING", "ACCEPTED", "COMPLETED"])

        nearby: list[dict] = []

        for req in all_requests:
            # ACCEPTED / COMPLETED → only show if this worker won the job
            if req.status in ("ACCEPTED", "COMPLETED"):
                won = req.bids.filter(worker=worker_profile, is_accepted=True).exists()
                if not won:
                    continue

            distance_km = haversine_distance(w_lat, w_lon, req.latitude, req.longitude)

            # PENDING → only include if within radius
            if req.status == "PENDING" and distance_km > WORKER_RADIUS_KM:
                continue

            # Annotate the distance onto the object so the serializer can access it
            req.distance_km = distance_km
            nearby.append(req)

        # Sort nearest-first
        nearby.sort(key=lambda r: r.distance_km)

        serializer = ServiceRequestDashboardSerializer(nearby, many=True)
        return Response(
            {"count": len(nearby), "results": serializer.data},
            status=status.HTTP_200_OK,
        )


class SubmitBidView(APIView):
    """
    POST /api/requests/<request_id>/bid/

    Worker submits a bid on a PENDING ServiceRequest.

    Business rules enforced:
    - Request must be in PENDING status.
    - Worker can bid only once per request (duplicate bids are rejected).
    """

    permission_classes = [IsWorker]

    def post(self, request, request_id):
        try:
            worker_profile = request.user.worker_profile
        except WorkerProfile.DoesNotExist:
            return Response(
                {"detail": "Worker profile not found."},
                status=status.HTTP_404_NOT_FOUND,
            )

        service_req = get_object_or_404(ServiceRequest, id=request_id)

        if service_req.status != "PENDING":
            return Response(
                {"detail": "Bids can only be placed on PENDING service requests."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        # Duplicate bid guard
        if Bid.objects.filter(service_request=service_req, worker=worker_profile).exists():
            return Response(
                {"detail": "You have already placed a bid on this request."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        serializer = BidCreateSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        bid = serializer.save(service_request=service_req, worker=worker_profile)

        return Response(
            BidReadSerializer(bid).data,
            status=status.HTTP_201_CREATED,
        )


# ===========================================================================
# D. Secure QR Completion & Review Endpoints
# ===========================================================================

class GenerateQRView(APIView):
    """
    GET /api/jobs/<request_id>/qr/

    Worker requests a QR code for an ACCEPTED job they won.
    Returns the scan URL and a Base64-encoded PNG QR code image
    (encoded as a data URI ready for <img src="...">) if the qrcode
    library is installed.
    """

    permission_classes = [IsWorker]

    def get(self, request, request_id):
        try:
            worker_profile = request.user.worker_profile
        except WorkerProfile.DoesNotExist:
            return Response({"detail": "Worker profile not found."}, status=status.HTTP_404_NOT_FOUND)

        service_req = get_object_or_404(
            ServiceRequest,
            id=request_id,
            status="ACCEPTED",
            bids__worker=worker_profile,
            bids__is_accepted=True,
        )

        # Build the completion URL that the customer will scan
        scan_url = request.build_absolute_uri(
            f"/api/jobs/complete/"
        )
        # Embed params in the URL for convenience
        completion_url = (
            f"{request.scheme}://{request.get_host()}"
            f"/api/jobs/complete/?request_id={service_req.id}&secret_token={service_req.secret_token}"
        )

        response_data: dict = {
            "request_id": service_req.id,
            "completion_url": completion_url,
            "qr_image_base64": None,
        }

        try:
            import qrcode  # type: ignore

            qr = qrcode.QRCode(version=1, box_size=10, border=4)
            qr.add_data(completion_url)
            qr.make(fit=True)
            img = qr.make_image(fill_color="black", back_color="white")
            buf = BytesIO()
            img.save(buf, format="PNG")
            response_data["qr_image_base64"] = (
                "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode("utf-8")
            )
        except ImportError:
            response_data["qr_image_base64"] = None
            response_data["note"] = "Install 'qrcode[pil]' to enable QR image generation."

        return Response(response_data, status=status.HTTP_200_OK)


class CompleteJobView(APIView):
    """
    POST /api/jobs/complete/

    Body: { "request_id": <int>, "secret_token": "<uuid>" }

    Validates:
    1. The caller is the Customer who owns the request.
    2. The provided secret_token matches the request's stored token.
    3. The request status is currently 'ACCEPTED'.

    On success → shifts status to 'COMPLETED'.
    """

    permission_classes = [IsCustomer]

    def post(self, request):
        serializer = JobCompleteSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        request_id = serializer.validated_data["request_id"]
        secret_token = serializer.validated_data["secret_token"]

        # All three conditions checked atomically in one query
        service_req = get_object_or_404(
            ServiceRequest,
            id=request_id,
            customer=request.user,  # ownership check
        )

        # Explicit status/token checks to provide clear error messages
        if str(service_req.secret_token) != str(secret_token):
            return Response(
                {"detail": "Invalid secret token. QR code may be expired or tampered with."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        if service_req.status != "ACCEPTED":
            return Response(
                {
                    "detail": (
                        f"Job cannot be completed. Current status is '{service_req.status}'. "
                        "Only ACCEPTED jobs can be completed."
                    )
                },
                status=status.HTTP_400_BAD_REQUEST,
            )

        service_req.status = "COMPLETED"
        service_req.save(update_fields=["status"])

        return Response(
            {
                "detail": "Job marked as COMPLETED successfully.",
                "request_id": service_req.id,
                "status": service_req.status,
            },
            status=status.HTTP_200_OK,
        )


class SubmitReviewView(APIView):
    """
    POST /api/jobs/<request_id>/review/

    Customer submits a 1–5 rating and optional comment for a COMPLETED job.

    Business rules:
    - Caller must be the Customer who owns the ServiceRequest.
    - Request must have status 'COMPLETED'.
    - A review can only be submitted once (OneToOne relationship).
    """

    permission_classes = [IsCustomer]

    def post(self, request, request_id):
        service_req = get_object_or_404(
            ServiceRequest,
            id=request_id,
            customer=request.user,
        )

        if service_req.status != "COMPLETED":
            return Response(
                {"detail": "Reviews can only be submitted for COMPLETED jobs."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        # Duplicate review guard (OneToOne enforced at DB level, but friendly message is better UX)
        if hasattr(service_req, "review"):
            return Response(
                {"detail": "A review has already been submitted for this job."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        # Retrieve the winning bid to link the worker
        winning_bid = get_object_or_404(
            Bid,
            service_request=service_req,
            is_accepted=True,
        )

        serializer = ReviewSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        review = serializer.save(
            service_request=service_req,
            worker=winning_bid.worker,
            customer=request.user,
        )

        return Response(ReviewSerializer(review).data, status=status.HTTP_201_CREATED)

