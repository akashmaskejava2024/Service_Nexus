package com.nt.service_nexus_android.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.nt.service_nexus_android.data.models.ServiceRequest;
import com.nt.service_nexus_android.databinding.ItemServiceRequestBinding;
import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private List<ServiceRequest> requests = new ArrayList<>();
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAcceptBid(int bidId);
        void onScanQr(int requestId);
    }

    public RequestAdapter(OnRequestActionListener listener) {
        this.listener = listener;
    }

    public void setRequests(List<ServiceRequest> newRequests) {
        this.requests = newRequests != null ? newRequests : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceRequestBinding binding = ItemServiceRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RequestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        holder.bind(requests.get(position));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private final ItemServiceRequestBinding binding;

        public RequestViewHolder(ItemServiceRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ServiceRequest req) {
            binding.tvRequestId.setText("Request #" + req.getId());
            binding.tvStatus.setText(req.getStatus());
            binding.tvDescription.setText(req.getDescription());

            if (req.getPhoto() != null && !req.getPhoto().trim().isEmpty()) {
                binding.ivPhoto.setVisibility(View.VISIBLE);
                Picasso.get().load(req.getPhoto()).into(binding.ivPhoto);
            } else {
                binding.ivPhoto.setVisibility(View.GONE);
            }

            if ("ACCEPTED".equalsIgnoreCase(req.getStatus())) {
                binding.btnScanQr.setVisibility(View.VISIBLE);
                binding.btnScanQr.setOnClickListener(v -> listener.onScanQr(req.getId()));
            } else {
                binding.btnScanQr.setVisibility(View.GONE);
            }

            int bidsCount = req.getBids().size();
            binding.tvBidsHeader.setText("Worker Bids (" + bidsCount + ")");

            if (bidsCount > 0) {
                binding.rvBids.setVisibility(View.VISIBLE);
                binding.rvBids.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                BidAdapter bidAdapter = new BidAdapter(bidId -> listener.onAcceptBid(bidId));
                binding.rvBids.setAdapter(bidAdapter);
                bidAdapter.setBids(req.getBids());
            } else {
                binding.rvBids.setVisibility(View.GONE);
            }
        }
    }
}
