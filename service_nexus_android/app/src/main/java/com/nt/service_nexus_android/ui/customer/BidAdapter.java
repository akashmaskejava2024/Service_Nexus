package com.nt.service_nexus_android.ui.customer;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nt.service_nexus_android.data.models.Bid;
import com.nt.service_nexus_android.databinding.ItemBidBinding;
import java.util.ArrayList;
import java.util.List;

public class BidAdapter extends RecyclerView.Adapter<BidAdapter.BidViewHolder> {
    private List<Bid> bids = new ArrayList<>();
    private final OnBidAcceptClickListener listener;

    public interface OnBidAcceptClickListener {
        void onAcceptBid(int bidId);
    }

    public BidAdapter(OnBidAcceptClickListener listener) {
        this.listener = listener;
    }

    public void setBids(List<Bid> newBids) {
        this.bids = newBids != null ? newBids : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBidBinding binding = ItemBidBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BidViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BidViewHolder holder, int position) {
        holder.bind(bids.get(position));
    }

    @Override
    public int getItemCount() {
        return bids.size();
    }

    class BidViewHolder extends RecyclerView.ViewHolder {
        private final ItemBidBinding binding;

        public BidViewHolder(ItemBidBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Bid bid) {
            binding.tvWorkerName.setText(bid.getWorker_username());
            binding.tvBidAmount.setText("₹" + bid.getAmount());
            binding.tvBidMessage.setText(bid.getMessage());

            if (bid.isIs_accepted()) {
                binding.btnAcceptBid.setText("ACCEPTED");
                binding.btnAcceptBid.setEnabled(false);
            } else {
                binding.btnAcceptBid.setText("Accept Bid");
                binding.btnAcceptBid.setEnabled(true);
                binding.btnAcceptBid.setOnClickListener(v -> listener.onAcceptBid(bid.getId()));
            }
        }
    }
}
