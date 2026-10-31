package com.nt.service_nexus_android.ui.worker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nt.service_nexus_android.data.models.NearbyRequest;
import com.nt.service_nexus_android.databinding.ItemRadarRequestBinding;
import java.util.ArrayList;
import java.util.List;

public class RadarAdapter extends RecyclerView.Adapter<RadarAdapter.RadarViewHolder> {
    private List<NearbyRequest> requests = new ArrayList<>();
    private final OnRadarActionListener listener;

    public interface OnRadarActionListener {
        void onPlaceBid(NearbyRequest request);
        void onShowQr(NearbyRequest request);
    }

    public RadarAdapter(OnRadarActionListener listener) {
        this.listener = listener;
    }

    public void setRequests(List<NearbyRequest> newRequests) {
        this.requests = newRequests != null ? newRequests : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RadarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRadarRequestBinding binding = ItemRadarRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RadarViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RadarViewHolder holder, int position) {
        holder.bind(requests.get(position));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class RadarViewHolder extends RecyclerView.ViewHolder {
        private final ItemRadarRequestBinding binding;

        public RadarViewHolder(ItemRadarRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(NearbyRequest request) {
            binding.tvCustomerName.setText(request.getCustomer_username() + " - Request #" + request.getId());
            binding.tvDistance.setText(request.getDistance_km() + " km away");
            binding.tvDescription.setText(request.getDescription());

            if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
                binding.btnPlaceBid.setVisibility(View.GONE);
                binding.btnShowQr.setVisibility(View.VISIBLE);
                binding.btnShowQr.setOnClickListener(v -> listener.onShowQr(request));
            } else {
                binding.btnPlaceBid.setVisibility(View.VISIBLE);
                binding.btnShowQr.setVisibility(View.GONE);
                binding.btnPlaceBid.setOnClickListener(v -> listener.onPlaceBid(request));
            }
        }
    }
}
