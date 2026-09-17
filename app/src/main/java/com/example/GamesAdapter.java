package com.example;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * GamesAdapter displays the user's gaming library with Play actions,
 * favorites, and per-game profile editing.
 */
public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.GameViewHolder> {

    public interface GameActionListener {
        void onPlayClicked(GameItem game);
        void onProfileClicked(GameItem game);
        void onFavoriteToggled(GameItem game);
        void onRemoveClicked(GameItem game);
    }

    private final List<GameItem> items = new ArrayList<>();
    private final GameActionListener listener;

    public GamesAdapter(GameActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<GameItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_card, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvPackage;
        final TextView tvStatus;
        final TextView tvLastPlayed;
        final ImageView btnFavorite;
        final ImageView btnEditProfile;
        final ImageView btnRemove;
        final MaterialButton btnPlay;

        GameViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_game_icon);
            tvTitle = itemView.findViewById(R.id.tv_game_title);
            tvPackage = itemView.findViewById(R.id.tv_game_package);
            tvStatus = itemView.findViewById(R.id.tv_game_status);
            tvLastPlayed = itemView.findViewById(R.id.tv_game_last_played);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            btnEditProfile = itemView.findViewById(R.id.btn_edit_profile);
            btnRemove = itemView.findViewById(R.id.btn_remove_game);
            btnPlay = itemView.findViewById(R.id.btn_play_game);
        }

        void bind(final GameItem item, final GameActionListener listener) {
            tvTitle.setText(item.getTitle());
            tvPackage.setText(item.getPackageName());
            ivIcon.setImageDrawable(item.getIcon());

            if (!item.isInstalled()) {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("Not Installed");
                btnPlay.setText("NOT INSTALLED");
                btnPlay.setEnabled(false);
                btnPlay.setAlpha(0.5f);
            } else {
                tvStatus.setVisibility(View.GONE);
                btnPlay.setText("PLAY");
                btnPlay.setEnabled(true);
                btnPlay.setAlpha(1.0f);
            }

            tvLastPlayed.setText(item.getLastPlayedFormatted());

            if (item.isFavorite()) {
                btnFavorite.setImageResource(R.drawable.ic_star_filled);
                btnFavorite.setColorFilter(itemView.getContext().getColor(R.color.accent_yellow));
            } else {
                btnFavorite.setImageResource(R.drawable.ic_star_outline);
                btnFavorite.setColorFilter(itemView.getContext().getColor(R.color.text_dim));
            }

            btnPlay.setOnClickListener(v -> {
                if (listener != null) listener.onPlayClicked(item);
            });

            btnFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onFavoriteToggled(item);
            });

            btnEditProfile.setOnClickListener(v -> {
                if (listener != null) listener.onProfileClicked(item);
            });

            btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onRemoveClicked(item);
            });
        }
    }
}
