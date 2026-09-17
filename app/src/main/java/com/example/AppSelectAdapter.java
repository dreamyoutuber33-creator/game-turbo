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
import java.util.Locale;

/**
 * AppSelectAdapter lists installed apps with live filtering for adding to library.
 */
public class AppSelectAdapter extends RecyclerView.Adapter<AppSelectAdapter.ViewHolder> {

    public interface AppSelectListener {
        void onAppSelected(GameItem item);
    }

    private final List<GameItem> originalList = new ArrayList<>();
    private final List<GameItem> filteredList = new ArrayList<>();
    private final AppSelectListener listener;

    public AppSelectAdapter(List<GameItem> apps, AppSelectListener listener) {
        this.listener = listener;
        if (apps != null) {
            this.originalList.addAll(apps);
            this.filteredList.addAll(apps);
        }
    }

    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String lower = query.toLowerCase(Locale.ROOT).trim();
            for (GameItem item : originalList) {
                if (item.getTitle().toLowerCase(Locale.ROOT).contains(lower) ||
                        item.getPackageName().toLowerCase(Locale.ROOT).contains(lower)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_select, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameItem item = filteredList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvPackage.setText(item.getPackageName());
        holder.ivIcon.setImageDrawable(item.getIcon());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAppSelected(item);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvPackage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_app_icon);
            tvTitle = itemView.findViewById(R.id.tv_app_title);
            tvPackage = itemView.findViewById(R.id.tv_app_package);
        }
    }
}
