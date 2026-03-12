from django.urls import path
from . import views, qr_views

urlpatterns = [
    path('register/customer/', views.register_customer, name='register_customer'),
    path('register/worker/', views.register_worker, name='register_worker'),
    path('login/', views.login_view, name='login'),
    path('logout/', views.logout_view, name='logout'),
    path('', views.map_view, name='map_view'),
    path('my-requests/', views.my_requests, name='my_requests'),
    path('accept-bid/<int:bid_id>/', views.accept_bid, name='accept_bid'),
    
    # Worker Views
    path('worker-dashboard/', views.worker_dashboard, name='worker_dashboard'),
    path('submit-bid/<int:request_id>/', views.submit_bid, name='submit_bid'),
    
    # QR & Job Completion Features
    path('generate-qr/<int:request_id>/', qr_views.generate_job_qr, name='generate_qr'),
    path('complete-job/<int:request_id>/<uuid:secret_token>/', views.complete_job_view, name='complete_job'),
]
