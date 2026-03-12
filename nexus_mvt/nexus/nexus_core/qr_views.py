try:
    import qrcode
    HAS_QRCODE = True
except ImportError:
    HAS_QRCODE = False

import base64
from io import BytesIO
from django.shortcuts import render, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.urls import reverse
from .models import ServiceRequest

@login_required
def generate_job_qr(request, request_id):
    """
    Generates a secure QR code for an accepted job.
    Accessible only by the Worker who won the bid.
    """
    if request.user.user_type != 'WORKER':
        return redirect('map_view')
        
    try:
        worker_profile = request.user.worker_profile
    except:
        return redirect('login')

    # Ensure this request exists, is ACCEPTED, and belongs to a bid THIS worker won
    service_req = get_object_or_404(
        ServiceRequest, 
        id=request_id, 
        status='ACCEPTED', 
        bids__worker=worker_profile, 
        bids__is_accepted=True
    )
    
    # Construct the absolute URL the customer needs to scan
    scan_url = request.build_absolute_uri(
        reverse('complete_job', kwargs={'request_id': service_req.id, 'secret_token': service_req.secret_token})
    )
    
    # Generate QR Code image if library is installed
    img_str = ""
    if HAS_QRCODE:
        qr = qrcode.QRCode(version=1, box_size=10, border=4)
        qr.add_data(scan_url)
        qr.make(fit=True)
        img = qr.make_image(fill_color="black", back_color="white")
        
        # Convert image to base64 so we can render it straight in the template without saving to disk
        buffer = BytesIO()
        img.save(buffer, format="PNG")
        img_str = base64.b64encode(buffer.getvalue()).decode("utf-8")
    
    context = {
        'service_request': service_req,
        'qr_image': img_str,
        'has_qrcode': HAS_QRCODE,
        'scan_url': scan_url
    }
    return render(request, 'nexus_core/qr_view.html', context)
