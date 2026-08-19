package com.example.currentlearnlayuot.viewload;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currentlearnlayuot.R;

import java.util.List;

/**
 * Adapter 触发方式演示用 RecyclerView.Adapter。
 *
 * 在 onCreateViewHolder 中：
 * 1. 打印传入的 parent（即 RecyclerView）。
 * 2. 使用带 Factory2 的 LayoutInflater inflate item 布局。
 * 在 onBindViewHolder 中绑定数据。
 * 在 onViewAttachedToWindow 中确认 item 已挂到 RecyclerView。
 */
public class LoadDemoAdapter extends RecyclerView.Adapter<LoadDemoAdapter.ViewHolder> {

    private final List<String> data;
    private final int itemLayoutRes;

    public LoadDemoAdapter(List<String> data, int itemLayoutRes) {
        this.data = data;
        this.itemLayoutRes = itemLayoutRes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewLoadLogger.log(ViewLoadLogger.Trigger.ADAPTER, ViewLoadLogger.Stage.INFLATE,
                "Adapter", "onCreateViewHolder 被调用，parent=" + parent.getClass().getName());

        // 使用 ContextThemeWrapper 避免与 AppCompat Factory 冲突
        ContextThemeWrapper wrapper = new ContextThemeWrapper(parent.getContext(), parent.getContext().getTheme());
        LayoutInflater inflater = LayoutInflater.from(wrapper);
        inflater.setFactory2(new InflateLoggerFactory(ViewLoadLogger.Trigger.ADAPTER));

        View itemView = inflater.inflate(itemLayoutRes, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvItem.setText(data.get(position));
        ViewLoadLogger.log(ViewLoadLogger.Trigger.ADAPTER, ViewLoadLogger.Stage.ATTACH,
                "Adapter", "onBindViewHolder position=" + position);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewParent parent = holder.itemView.getParent();
        String parentName = parent == null ? "null" : parent.getClass().getSimpleName();
        ViewLoadLogger.log(ViewLoadLogger.Trigger.ADAPTER, ViewLoadLogger.Stage.ATTACH,
                "Adapter", "item 已 attach 到 parent=" + parentName);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItem = itemView.findViewById(R.id.tvItem);
        }
    }
}
