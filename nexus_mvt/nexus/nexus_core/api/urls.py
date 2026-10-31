"""
urls.py — API URL routing for Service Nexus DRF layer.

All routes are prefixed with /api/ by the root urls.py.

Pattern overview
----------------
/api/auth/register/customer/        → RegisterCustomerView
/api/auth/register/worker/          → RegisterWorkerView
/api/auth/login/                    → LoginView
/api/auth/token/refresh/            → simplejwt TokenRefreshView

/api/requests/                      → ServiceRequestCreateView   (POST)
/api/requests/my-requests/          → MyRequestsView              (GET)
/api/requests/<id>/bid/             → SubmitBidView               (POST)

/api/bids/<id>/accept/              → AcceptBidView               (POST)

/api/worker/dashboard/              → WorkerDashboardView         (GET)

/api/jobs/<id>/qr/                  → GenerateQRView              (GET)
/api/jobs/complete/                 → CompleteJobView             (POST)
/api/jobs/<id>/review/              → SubmitReviewView            (POST)
"""

from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView

from .views import (
    # Auth
    RegisterCustomerView,
    RegisterWorkerView,
    LoginView,
    # Customer
    ServiceRequestCreateView,
    MyRequestsView,
    AcceptBidView,
    # Worker
    WorkerDashboardView,
    SubmitBidView,
    # Jobs & Reviews
    GenerateQRView,
    CompleteJobView,
    SubmitReviewView,
)

# ---------------------------------------------------------------------------
# Auth URLs
# ---------------------------------------------------------------------------
auth_patterns = [
    path("register/customer/", RegisterCustomerView.as_view(), name="api_register_customer"),
    path("register/worker/", RegisterWorkerView.as_view(), name="api_register_worker"),
    path("login/", LoginView.as_view(), name="api_login"),
    path("token/refresh/", TokenRefreshView.as_view(), name="api_token_refresh"),
]

# ---------------------------------------------------------------------------
# Request / Bid URLs
# ---------------------------------------------------------------------------
request_patterns = [
    path("", ServiceRequestCreateView.as_view(), name="api_service_request_create"),
    path("my-requests/", MyRequestsView.as_view(), name="api_my_requests"),
    path("<int:request_id>/bid/", SubmitBidView.as_view(), name="api_submit_bid"),
]

bid_patterns = [
    path("<int:bid_id>/accept/", AcceptBidView.as_view(), name="api_accept_bid"),
]

# ---------------------------------------------------------------------------
# Worker URLs
# ---------------------------------------------------------------------------
worker_patterns = [
    path("dashboard/", WorkerDashboardView.as_view(), name="api_worker_dashboard"),
]

# ---------------------------------------------------------------------------
# Job & Review URLs
# ---------------------------------------------------------------------------
job_patterns = [
    path("complete/", CompleteJobView.as_view(), name="api_complete_job"),
    path("<int:request_id>/qr/", GenerateQRView.as_view(), name="api_generate_qr"),
    path("<int:request_id>/review/", SubmitReviewView.as_view(), name="api_submit_review"),
]

# ---------------------------------------------------------------------------
# Combined — consumed by nexus/urls.py via include("nexus_core.api.urls")
# ---------------------------------------------------------------------------
from django.urls import include

urlpatterns = [
    path("auth/", include((auth_patterns, "auth"))),
    path("requests/", include((request_patterns, "requests"))),
    path("bids/", include((bid_patterns, "bids"))),
    path("worker/", include((worker_patterns, "worker"))),
    path("jobs/", include((job_patterns, "jobs"))),
]

