from django import forms
from django.contrib.auth.forms import UserCreationForm
from .models import User, WorkerProfile, ServiceRequest

class CustomerSignUpForm(UserCreationForm):
    class Meta(UserCreationForm.Meta):
        model = User
        fields = ('username', 'email')

    def save(self, commit=True):
        user = super().save(commit=False)
        user.user_type = 'CUSTOMER'
        if commit:
            user.save()
        return user

class WorkerSignUpForm(UserCreationForm):
    category = forms.ChoiceField(choices=WorkerProfile.CATEGORY_CHOICES, required=True)
    latitude = forms.FloatField(widget=forms.HiddenInput(), required=False)
    longitude = forms.FloatField(widget=forms.HiddenInput(), required=False)

    class Meta(UserCreationForm.Meta):
        model = User
        fields = ('username', 'email')

    def save(self, commit=True):
        user = super().save(commit=False)
        user.user_type = 'WORKER'
        if commit:
            user.save()
            WorkerProfile.objects.create(
                user=user,
                category=self.cleaned_data.get('category'),
                latitude=self.cleaned_data.get('latitude'),
                longitude=self.cleaned_data.get('longitude')
            )
        return user

class ServiceRequestForm(forms.ModelForm):
    latitude = forms.FloatField(widget=forms.HiddenInput())
    longitude = forms.FloatField(widget=forms.HiddenInput())

    class Meta:
        model = ServiceRequest
        fields = ['description', 'photo', 'latitude', 'longitude']

from .models import Review

class ReviewForm(forms.ModelForm):
    class Meta:
        model = Review
        fields = ['rating', 'comment']
