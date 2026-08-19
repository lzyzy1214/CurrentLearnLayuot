package com.example.currentlearnlayuot.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currentlearnlayuot.R;
import com.example.currentlearnlayuot.data.Chapter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节列表适配器，支持搜索过滤
 */
public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder> {

    private List<Chapter> allItems;
    private List<Chapter> filteredItems;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Chapter chapter);
    }

    public ChapterAdapter(List<Chapter> items, OnItemClickListener listener) {
        this.allItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
        this.listener = listener;
    }

    public void updateData(List<Chapter> items) {
        this.allItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        String lower = query == null ? "" : query.toLowerCase().trim();
        filteredItems.clear();
        if (lower.isEmpty()) {
            filteredItems.addAll(allItems);
        } else {
            for (Chapter chapter : allItems) {
                if (chapter.title.toLowerCase().contains(lower)
                        || chapter.summary.toLowerCase().contains(lower)) {
                    filteredItems.add(chapter);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chapter chapter = filteredItems.get(position);
        holder.tvNumber.setText(String.format("%02d", chapter.id));
        holder.tvTitle.setText(chapter.title);
        holder.tvSummary.setText(chapter.summary);
        holder.chipDifficulty.setText(chapter.difficulty);
        holder.card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(chapter);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView tvNumber;
        final TextView tvTitle;
        final TextView tvSummary;
        final Chip chipDifficulty;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            chipDifficulty = itemView.findViewById(R.id.chipDifficulty);
        }
    }
}
