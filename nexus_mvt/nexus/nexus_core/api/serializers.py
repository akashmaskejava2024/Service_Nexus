"""
serializers.py — DRF Serializers for Service Nexus API.

Covers:
  - User registration (Customer & Worker)
  - JWT login response
  - WorkerProfile
  - ServiceRequest (read + create)
  - Bid (read + create + accept)
  - Review (create)
"""

from django.contrib.auth import get_user_model
from django.contrib.auth.password_validation import validate_password
from rest_framework import serializers
from rest_framework_simplejwt.tokens import RefreshToken

from nexus_core.models import WorkerProfile, ServiceRequest, Bid, Review

User = get_user_model()


# ---------------------------------------------------------------------------
# Auth Serializers
# ---------------------------------------------------------------------------

class CustomerRegisterSerializer(serializers.ModelSerializer):
    """Validates and creates a CUSTOMER user account."""

    password = serializers.CharField(
        write_only=True, required=True, validators=[validate_password]
    )
    password2 = serializers.CharField(write_only=True, required=True, label="Confirm password")

    class Meta:
        model = User
        fields = ("id", "username", "email", "first_name", "last_name", "password", "password2")
        extra_kwargs = {
            "email": {"required": True},
            "first_name": {"required": False},
            "last_name": {"required": False},
        }

    def validate(self, attrs):
        if attrs["password"] != attrs["password2"]:
            raise serializers.ValidationError({"password": "Password fields did not match."})
        return attrs

    def create(self, validated_data):
        validated_data.pop("password2")
        user = User.objects.create_user(
            username=validated_data["username"],
            email=validated_data.get("email", ""),
            first_name=validated_data.get("first_name", ""),
            last_name=validated_data.get("last_name", ""),
            password=validated_data["password"],
            user_type="CUSTOMER",
        )
        return user


class WorkerRegisterSerializer(serializers.ModelSerializer):
    """Validates and creates a WORKER user account along with their WorkerProfile."""

    password = serializers.CharField(
        write_only=True, required=True, validators=[validate_password]
    )
    password2 = serializers.CharField(write_only=True, required=True, label="Confirm password")

    # WorkerProfile fields — collected at registration time
    category = serializers.ChoiceField(choices=WorkerProfile.CATEGORY_CHOICES, write_only=True)
    latitude = serializers.FloatField(write_only=True)
    longitude = serializers.FloatField(write_only=True)

    class Meta:
        model = User
        fields = (
            "id",
            "username",
            "email",
            "first_name",
            "last_name",
            "password",
            "password2",
            "category",
            "latitude",
            "longitude",
        )
        extra_kwargs = {"email": {"required": True}}

    def validate(self, attrs):
        if attrs["password"] != attrs["password2"]:
            raise serializers.ValidationError({"password": "Password fields did not match."})
        return attrs

    def create(self, validated_data):
        # Pop profile fields before creating the user
        category = validated_data.pop("category")
        latitude = validated_data.pop("latitude")
        longitude = validated_data.pop("longitude")
        validated_data.pop("password2")

        user = User.objects.create_user(
            username=validated_data["username"],
            email=validated_data.get("email", ""),
            first_name=validated_data.get("first_name", ""),
            last_name=validated_data.get("last_name", ""),
            password=validated_data["password"],
            user_type="WORKER",
        )
        WorkerProfile.objects.create(
            user=user, category=category, latitude=latitude, longitude=longitude
        )
        return user


class LoginSerializer(serializers.Serializer):
    """
    Validates the login payload — username + password only.
    Mirrors Django's AuthenticationForm used in the MVT project.
    """

    username = serializers.CharField(
        required=True,
        max_length=150,
        trim_whitespace=True,
        help_text="The username set at registration (not email).",
    )
    password = serializers.CharField(
        required=True,
        write_only=True,   # Never echo the password back in any response
        style={"input_type": "password"},
    )

    def validate_username(self, value):
        if not value:
            raise serializers.ValidationError("Username must not be blank.")
        return value

    def validate_password(self, value):
        if not value:
            raise serializers.ValidationError("Password must not be blank.")
        return value


class LoginResponseSerializer(serializers.Serializer):
    """Shape of the response body returned after a successful login."""

    refresh = serializers.CharField(read_only=True)
    access = serializers.CharField(read_only=True)
    user_id = serializers.IntegerField(read_only=True)
    username = serializers.CharField(read_only=True)
    user_type = serializers.CharField(read_only=True)


# ---------------------------------------------------------------------------
# WorkerProfile Serializer
# ---------------------------------------------------------------------------

class WorkerProfileSerializer(serializers.ModelSerializer):
    username = serializers.CharField(source="user.username", read_only=True)
    email = serializers.CharField(source="user.email", read_only=True)

    class Meta:
        model = WorkerProfile
        fields = ("id", "username", "email", "category", "latitude", "longitude")


# ---------------------------------------------------------------------------
# Bid Serializers
# ---------------------------------------------------------------------------

class BidReadSerializer(serializers.ModelSerializer):
    """Used when embedding bids inside a ServiceRequest response."""

    worker_username = serializers.CharField(source="worker.user.username", read_only=True)
    worker_category = serializers.CharField(source="worker.category", read_only=True)

    class Meta:
        model = Bid
        fields = (
            "id",
            "worker_username",
            "worker_category",
            "amount",
            "message",
            "is_accepted",
            "created_at",
        )


class BidCreateSerializer(serializers.ModelSerializer):
    """Used by a Worker to submit a new bid."""

    class Meta:
        model = Bid
        fields = ("id", "amount", "message")

    def validate_amount(self, value):
        if value <= 0:
            raise serializers.ValidationError("Bid amount must be a positive number.")
        return value


# ---------------------------------------------------------------------------
# ServiceRequest Serializers
# ---------------------------------------------------------------------------

class ServiceRequestCreateSerializer(serializers.ModelSerializer):
    """Handles creation of a new ServiceRequest by a Customer."""

    class Meta:
        model = ServiceRequest
        fields = ("id", "description", "photo", "latitude", "longitude", "status", "secret_token")
        read_only_fields = ("id", "status", "secret_token")

    def validate(self, attrs):
        lat = attrs.get("latitude")
        lon = attrs.get("longitude")
        if lat is None or not (-90 <= lat <= 90):
            raise serializers.ValidationError({"latitude": "Latitude must be between -90 and 90."})
        if lon is None or not (-180 <= lon <= 180):
            raise serializers.ValidationError(
                {"longitude": "Longitude must be between -180 and 180."}
            )
        return attrs


class ServiceRequestListSerializer(serializers.ModelSerializer):
    """
    Full representation of a ServiceRequest used when listing a customer's requests.
    Eagerly nests the associated bids so no N+1 queries occur (prefetch_related is
    called at the view level).
    """

    bids = BidReadSerializer(many=True, read_only=True)
    secret_token = serializers.UUIDField(read_only=True)

    class Meta:
        model = ServiceRequest
        fields = (
            "id",
            "description",
            "photo",
            "latitude",
            "longitude",
            "status",
            "secret_token",
            "created_at",
            "bids",
        )


class ServiceRequestDashboardSerializer(serializers.ModelSerializer):
    """
    Compact representation used on the Worker Dashboard.
    Includes the calculated distance (km) injected by the view.
    """

    distance_km = serializers.FloatField(read_only=True)
    customer_username = serializers.CharField(source="customer.username", read_only=True)
    bids_count = serializers.SerializerMethodField()

    class Meta:
        model = ServiceRequest
        fields = (
            "id",
            "description",
            "photo",
            "latitude",
            "longitude",
            "status",
            "created_at",
            "customer_username",
            "bids_count",
            "distance_km",
        )

    def get_bids_count(self, obj):
        return obj.bids.count()


# ---------------------------------------------------------------------------
# Job Completion Serializer
# ---------------------------------------------------------------------------

class JobCompleteSerializer(serializers.Serializer):
    """Payload required to mark a job as COMPLETED."""

    request_id = serializers.IntegerField()
    secret_token = serializers.UUIDField()


# ---------------------------------------------------------------------------
# Review Serializer
# ---------------------------------------------------------------------------

class ReviewSerializer(serializers.ModelSerializer):
    """Customer submits a rating and optional comment for a completed job."""

    class Meta:
        model = Review
        fields = ("id", "rating", "comment", "created_at")
        read_only_fields = ("id", "created_at")

    def validate_rating(self, value):
        if not (1 <= value <= 5):
            raise serializers.ValidationError("Rating must be an integer between 1 and 5.")
        return value

