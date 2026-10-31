"""
permissions.py — Custom DRF permission classes for Service Nexus API.

IsCustomer  — allows only authenticated CUSTOMER users through.
IsWorker    — allows only authenticated WORKER users through.
"""

from rest_framework.permissions import BasePermission, IsAuthenticated


class IsCustomer(BasePermission):
    """
    Allows access only to authenticated users whose user_type is 'CUSTOMER'.
    """

    message = "Only customer accounts are permitted to perform this action."

    def has_permission(self, request, view):
        return bool(
            request.user
            and request.user.is_authenticated
            and request.user.user_type == "CUSTOMER"
        )


class IsWorker(BasePermission):
    """
    Allows access only to authenticated users whose user_type is 'WORKER'.
    """

    message = "Only worker accounts are permitted to perform this action."

    def has_permission(self, request, view):
        return bool(
            request.user
            and request.user.is_authenticated
            and request.user.user_type == "WORKER"
        )

