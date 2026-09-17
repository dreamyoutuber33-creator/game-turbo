package com.example;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * LastPlayedAdapter displays compact game cards in the LAST PLAYED horizontal rail.
 */
public class LastPlayedAdapter extends RecyclerView.Adapter<LastPlayedAdapter.ViewHolder> {

    public interface ItemClickListener {
        void onItemClick(GameItem item);
    }

    private final List<GameItem> items = new ArrayList<>();
    private final ItemClickListener listener;

    public LastPlayedAdapter(ItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<GameItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_last_played, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvTime.setText(item.getLastPlayedFormatted());
        holder.ivIcon.setImageDrawable(item.getIcon());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_last_played_icon);
            tvTitle = itemView.findViewById(R.id.tv_last_played_title);
            tvTime = itemView.findViewById(R.id.tv_last_played_time);
        }
    }
}
