package com.nfcreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardHistoryAdapter extends RecyclerView.Adapter<CardHistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CardInfo cardInfo);
    }

    private List<CardInfo> cardList;
    private OnItemClickListener listener;

    public CardHistoryAdapter(List<CardInfo> cardList, OnItemClickListener listener) {
        this.cardList = cardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardInfo card = cardList.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvCardLabel.setText(card.getCardLabel());
        holder.tvCardType.setText(card.getCardTypeName());
        holder.tvUid.setText("UID: " + card.getUidFormatted());
        holder.tvTimestamp.setText(card.getTimestamp());
        holder.tvTechnology.setText(card.getTechnology());

        // Set card color
        int color = ContextCompat.getColor(ctx, card.getCardColorRes());
        holder.viewColorIndicator.setBackgroundColor(color);

        // Set icon
        holder.ivCardIcon.setImageResource(card.getCardIconRes());

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(card);
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewColorIndicator;
        ImageView ivCardIcon;
        TextView tvCardLabel;
        TextView tvCardType;
        TextView tvUid;
        TextView tvTimestamp;
        TextView tvTechnology;

        ViewHolder(View view) {
            super(view);
            viewColorIndicator = view.findViewById(R.id.view_color_indicator);
            ivCardIcon = view.findViewById(R.id.iv_card_icon);
            tvCardLabel = view.findViewById(R.id.tv_card_label);
            tvCardType = view.findViewById(R.id.tv_card_type);
            tvUid = view.findViewById(R.id.tv_uid);
            tvTimestamp = view.findViewById(R.id.tv_timestamp);
            tvTechnology = view.findViewById(R.id.tv_technology);
        }
    }
}
