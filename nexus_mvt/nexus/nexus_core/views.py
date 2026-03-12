from django.shortcuts import render, redirect, get_object_or_404
from django.contrib.auth import login, logout
from django.contrib.auth.forms import AuthenticationForm
from django.contrib.auth.decorators import login_required
from django.contrib import messages

from .models import User, WorkerProfile, ServiceRequest, Bid, Review
from .forms import CustomerSignUpForm, WorkerSignUpForm, ServiceRequestForm, ReviewForm
from .utils import calculate_haversine

# --- Authentication Views ---
def register_customer(request):
    if request.method == 'POST':
        form = CustomerSignUpForm(request.POST)
        if form.is_valid():
            user = form.save()
            login(request, user)
            return redirect('map_view')
    else:
        form = CustomerSignUpForm()
    return render(request, 'nexus_core/signup_customer.html', {'form': form})

def register_worker(request):
    if request.method == 'POST':
        form = WorkerSignUpForm(request.POST)
        if form.is_valid():
            user = form.save()
            login(request, user)
            return redirect('worker_dashboard')
    else:
        form = WorkerSignUpForm()
    return render(request, 'nexus_core/signup_worker.html', {'form': form})

def login_view(request):
    if request.method == 'POST':
        form = AuthenticationForm(request, data=request.POST)
        if form.is_valid():
            user = form.get_user()
            login(request, user)
            if user.user_type == 'WORKER':
                return redirect('worker_dashboard')
            return redirect('my_requests')
    else:
        form = AuthenticationForm()
    return render(request, 'nexus_core/login.html', {'form': form})

def logout_view(request):
    if request.method == 'POST':
        logout(request)
        return redirect('login')
    # If they visit via GET manually, offer a logout button snippet or just redirect
    return redirect('login')

# --- Customer Views ---
@login_required
def map_view(request):
    if request.user.user_type != 'CUSTOMER':
        return redirect('worker_dashboard')

    if request.method == 'POST':
        form = ServiceRequestForm(request.POST, request.FILES)
        if form.is_valid():
            service_request = form.save(commit=False)
            service_request.customer = request.user
            service_request.save()
            messages.success(request, "Your request has been posted! Workers in your area are being notified.")
            return redirect('my_requests')
        else:
            messages.error(request, "There was an error with your request. Please ensure coordinates and fields are valid.")
            
    return render(request, 'nexus_core/map.html')

@login_required
def my_requests(request):
    if request.user.user_type != 'CUSTOMER':
        return redirect('worker_dashboard')
    
    # Fetch customer's requests and prefetch related bids to display
    requests = ServiceRequest.objects.filter(customer=request.user).order_by('-created_at')
    return render(request, 'nexus_core/my_requests.html', {'requests': requests})

@login_required
def accept_bid(request, bid_id):
    if request.method == 'POST':
        bid = get_object_or_404(Bid, id=bid_id, service_request__customer=request.user)
        # Mark Bid as accepted
        bid.is_accepted = True
        bid.save()
        # Mark Service Request as accepted
        service_req = bid.service_request
        service_req.status = 'ACCEPTED'
        service_req.save()
        messages.success(request, f"You have accepted the bid from {bid.worker.user.username}.")
        return redirect('my_requests')
    return redirect('map_view')

# --- Worker Views ---
@login_required
def worker_dashboard(request):
    if request.user.user_type != 'WORKER':
        return redirect('map_view')
    
    try:
        worker_profile = request.user.worker_profile
    except WorkerProfile.DoesNotExist:
        messages.error(request, "Worker profile not found.")
        return redirect('logout')

    # Radius logic: Find pending AND accepted/completed requests within 5km
    # Include ACCEPTED/COMPLETED so workers can see the jobs they won to generate QRs or see history
    relevant_requests = ServiceRequest.objects.filter(status__in=['PENDING', 'ACCEPTED', 'COMPLETED'])
    nearby_requests = []
    
    if worker_profile.latitude and worker_profile.longitude:
        for req in relevant_requests:
            
            # If accepted or completed, ONLY show it if THIS worker won it
            if req.status in ['ACCEPTED', 'COMPLETED']:
                won = req.bids.filter(worker=worker_profile, is_accepted=True).exists()
                if not won:
                    continue # Skip showing other workers' won jobs
            
            distance = calculate_haversine(
                worker_profile.latitude, 
                worker_profile.longitude, 
                req.latitude, 
                req.longitude
            )
            if distance <= 5.0: # 5km radius
                nearby_requests.append({'request': req, 'distance': round(distance, 2)})
    
    # Sort by nearest first
    nearby_requests.sort(key=lambda x: x['distance'])

    return render(request, 'nexus_core/worker_dashboard.html', {'nearby_requests': nearby_requests})

@login_required
def submit_bid(request, request_id):
    if request.user.user_type != 'WORKER':
        return redirect('map_view')
        
    service_req = get_object_or_404(ServiceRequest, id=request_id, status='PENDING')
    worker_profile = request.user.worker_profile
    
    # Check if already bid
    if Bid.objects.filter(service_request=service_req, worker=worker_profile).exists():
        messages.error(request, "You have already placed a bid on this request.")
        return redirect('worker_dashboard')

    if request.method == 'POST':
        amount = request.POST.get('amount')
        message = request.POST.get('message', '')
        
        if amount:
            Bid.objects.create(
                service_request=service_req,
                worker=worker_profile,
                amount=amount,
                message=message
            )
            messages.success(request, "Bid submitted successfully!")
            return redirect('worker_dashboard')
            
    return render(request, 'nexus_core/submit_bid.html', {'service_request': service_req})

# --- Job Completion & Feedback Views ---
@login_required
def complete_job_view(request, request_id, secret_token):
    """
    Scanned by the Customer. Validates token, marks job as complete, and asks for feedback.
    """
    if request.user.user_type != 'CUSTOMER':
        messages.error(request, "Only the customer who requested the service can scan to complete.")
        return redirect('worker_dashboard')

    service_req = get_object_or_404(
        ServiceRequest, 
        id=request_id, 
        secret_token=secret_token,
        customer=request.user,
        status='ACCEPTED'
    )
    
    # Retrieve the winning bid
    winning_bid = get_object_or_404(Bid, service_request=service_req, is_accepted=True)
    worker_profile = winning_bid.worker

    if request.method == 'POST':
        form = ReviewForm(request.POST)
        if form.is_valid():
            review = form.save(commit=False)
            review.service_request = service_req
            review.worker = worker_profile
            review.customer = request.user
            review.save()
            
            # finalize the request status
            service_req.status = 'COMPLETED'
            service_req.save()
            
            messages.success(request, f"Job Completed! Thank you for reviewing {worker_profile.user.username}.")
            return redirect('my_requests')
    else:
        form = ReviewForm()

    context = {
        'service_request': service_req,
        'worker': worker_profile,
        'form': form,
    }
    return render(request, 'nexus_core/leave_feedback.html', context)
