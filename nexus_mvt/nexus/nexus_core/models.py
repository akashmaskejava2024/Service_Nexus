from django.contrib.auth.models import AbstractUser
from django.db import models
from django.conf import settings

class User(AbstractUser):
    ROLE_CHOICES = (
        ('CUSTOMER', 'Customer'),
        ('WORKER', 'Worker'),
    )
    user_type = models.CharField(max_length=20, choices=ROLE_CHOICES, default='CUSTOMER')

class WorkerProfile(models.Model):
    CATEGORY_CHOICES = (
        ('PLUMBER', 'Plumber'),
        ('ELECTRICIAN', 'Electrician'),
        ('CARPENTER', 'Carpenter'),
        ('CLEANER', 'Cleaner'),
        ('OTHER', 'Other'),
    )
    user = models.OneToOneField(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='worker_profile')
    category = models.CharField(max_length=50, choices=CATEGORY_CHOICES)
    latitude = models.FloatField(null=True, blank=True)
    longitude = models.FloatField(null=True, blank=True)

    def __str__(self):
        return f"{self.user.username} - {self.category}"

import uuid

class ServiceRequest(models.Model):
    customer = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='service_requests')
    description = models.TextField()
    latitude = models.FloatField()
    longitude = models.FloatField()
    photo = models.FileField(upload_to='service_photos/', null=True, blank=True) # Changed from ImageField to bypass Pillow dependency offline
    created_at = models.DateTimeField(auto_now_add=True)
    secret_token = models.UUIDField(default=uuid.uuid4, editable=False, unique=True)
    
    STATUS_CHOICES = (
        ('PENDING', 'Pending'),
        ('ACCEPTED', 'Accepted'),
        ('COMPLETED', 'Completed'),
    )
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='PENDING')

    def __str__(self):
        return f"Request by {self.customer.username} at {self.created_at.strftime('%Y-%m-%d %H:%M')}"

class Bid(models.Model):
    service_request = models.ForeignKey(ServiceRequest, on_delete=models.CASCADE, related_name='bids')
    worker = models.ForeignKey(WorkerProfile, on_delete=models.CASCADE, related_name='bids')
    amount = models.DecimalField(max_digits=10, decimal_places=2)
    message = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    is_accepted = models.BooleanField(default=False)

    def __str__(self):
        return f"Bid by {self.worker.user.username} for {self.amount}"

class Review(models.Model):
    service_request = models.OneToOneField(ServiceRequest, on_delete=models.CASCADE, related_name='review')
    worker = models.ForeignKey(WorkerProfile, on_delete=models.CASCADE, related_name='reviews')
    customer = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='reviews_given')
    rating = models.IntegerField(choices=[(i, i) for i in range(1, 6)])
    comment = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.rating}/5 for {self.worker.user.username} by {self.customer.username}"
