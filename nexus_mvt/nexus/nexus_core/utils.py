import math

def calculate_haversine(lat1, lon1, lat2, lon2):
    """
    Calculates the great-circle distance between two points
    on the Earth surface using their latitude and longitude in kilometers.
    """
    R = 6371.0 # Radius of Earth in km

    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)

    a = math.sin(delta_phi / 2.0)**2 + \
        math.cos(phi1) * math.cos(phi2) * \
        math.sin(delta_lambda / 2.0)**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

    distance = R * c
    return distance
