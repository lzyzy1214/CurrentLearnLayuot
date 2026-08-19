package com.example.currentlearnlayuot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 用于验证 Adapter 入口的 RecyclerView.Adapter。
 * 在 onCreateViewHolder 中打印 parent 信息并回调到页面日志区。
 */
public class ProofDemoAdapter extends RecyclerView.Adapter<ProofDemoAdapter.ViewHolder> {

    private final List<String> data;
    private final OnLogCallback logCallback;

    /**
     * 日志回调接口
     */
    public interface OnLogCallback {
        void onLog(String log);
    }

    public ProofDemoAdapter(List<String> data, OnLogCallback logCallback) {
        this.data = data;
        this.logCallback = logCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        String parentInfo = "Adapter parent: " + parent.getClass().getName();
        log(parentInfo);

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_proof_demo, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvItem.setText(data.get(position));
        log("onBindViewHolder position=" + position);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // itemView 真正被 RecyclerView 附加到窗口时才能拿到父容器
        ViewParent itemParent = holder.itemView.getParent();
        String parentName = itemParent != null ? itemParent.getClass().getSimpleName() : "null";
        log("item attached to parent=" + parentName);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void log(String message) {
        if (logCallback != null) {
            logCallback.onLog(message);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItem = itemView.findViewById(R.id.tvItem);
        }
    }
}
