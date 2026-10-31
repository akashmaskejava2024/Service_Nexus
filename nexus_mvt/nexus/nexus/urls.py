from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('admin/', admin.site.urls),

    # -----------------------------------------------------------------------
    # DRF API layer — all endpoints live under /api/
    # -----------------------------------------------------------------------
    path('api/', include('nexus_core.api.urls')),

    # -----------------------------------------------------------------------
    # Legacy MVT views (keep working alongside the API)
    # -----------------------------------------------------------------------
    path('', include('nexus_core.urls')),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
