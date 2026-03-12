from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from .models import User, WorkerProfile, ServiceRequest, Bid, Review

class CustomUserAdmin(UserAdmin):
    model = User
    fieldsets = UserAdmin.fieldsets + (
        (None, {'fields': ('user_type',)}),
    )
    add_fieldsets = UserAdmin.add_fieldsets + (
        (None, {'fields': ('user_type',)}),
    )

admin.site.register(User, CustomUserAdmin)
admin.site.register(WorkerProfile)
admin.site.register(ServiceRequest)
admin.site.register(Bid)
admin.site.register(Review)
