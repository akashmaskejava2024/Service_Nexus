package com.nt.service_nexus_android.ui.worker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.nt.service_nexus_android.databinding.DialogBiddingBinding;

public class BiddingBottomSheetDialog extends BottomSheetDialogFragment {
    private DialogBiddingBinding binding;
    private final OnBidSubmitListener listener;

    public interface OnBidSubmitListener {
        void onSubmitBid(String amount, String message);
    }

    public BiddingBottomSheetDialog(OnBidSubmitListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogBiddingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSubmitBid.setOnClickListener(v -> {
            String amount = binding.etBidAmount.getText() != null ? binding.etBidAmount.getText().toString() : "";
            String message = binding.etBidMessage.getText() != null ? binding.etBidMessage.getText().toString() : "";
            if (listener != null) {
                listener.onSubmitBid(amount, message);
            }
            dismiss();
        });
    }
}
