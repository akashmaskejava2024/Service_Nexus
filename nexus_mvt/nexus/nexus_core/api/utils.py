"""
utils.py — Shared utility functions for the Service Nexus DRF API.

Provides the Haversine great-circle distance formula used for
proximity-based worker dashboard filtering.
"""

import math


def haversine_distance(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """
    Calculate the great-circle distance in kilometres between two points
    on the Earth using the Haversine formula.

    Args:
        lat1: Latitude of point 1 in decimal degrees.
        lon1: Longitude of point 1 in decimal degrees.
        lat2: Latitude of point 2 in decimal degrees.
        lon2: Longitude of point 2 in decimal degrees.

    Returns:
        Distance in kilometres (float), rounded to 4 decimal places.

    Example:
        >>> haversine_distance(28.6139, 77.2090, 28.7041, 77.1025)
        13.3...
    """
    R = 6371.0  # Earth's mean radius in km

    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    d_phi = math.radians(lat2 - lat1)
    d_lambda = math.radians(lon2 - lon1)

    a = (
        math.sin(d_phi / 2.0) ** 2
        + math.cos(phi1) * math.cos(phi2) * math.sin(d_lambda / 2.0) ** 2
    )
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

    return round(R * c, 4)


# Radius constant used throughout the worker proximity filter.
WORKER_RADIUS_KM = 5.0

