package com.example.currentlearnlayuot.viewload;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currentlearnlayuot.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter 触发方式演示页。
 *
 * 流程：
 * 1. Activity content 中动态创建 RecyclerView。
 * 2. RecyclerView 调用 Adapter.onCreateViewHolder。
 * 3. Adapter 内部使用 LayoutInflater + Factory2 inflate item 布局。
 * 4. item View 被 add 到 RecyclerView，随后进入 Measure / Layout / Draw。
 */
public class AdapterLoadActivity extends BaseLoadActivity {

    private RecyclerView recyclerView;

    @Override
    protected String getPageTitle() {
        return "Adapter.onCreateViewHolder";
    }

    @Override
    protected String getProcessDescription() {
        return "1. RecyclerView 准备显示列表项\n" +
                "2. 调用 Adapter.onCreateViewHolder(parent=RecyclerView, viewType)\n" +
                "3. LayoutInflater 解析 item 布局，Factory2 拦截每个标签\n" +
                "4. 创建 ViewHolder 并返回给 RecyclerView\n" +
                "5. onBindViewHolder 绑定数据，onViewAttachedToWindow 确认挂到 RecyclerView\n" +
                "6. 每个 item 进入 Measure → Layout → Draw 渲染到屏幕";
    }

    @Override
    protected void runLoadFlow() {
        int itemLayoutRes = isAlternate ? R.layout.item_adapter_load_b : R.layout.item_adapter_load_a;

        // 1. 编译期
        ViewLoadLogger.log(ViewLoadLogger.Trigger.ADAPTER, ViewLoadLogger.Stage.COMPILE,
                getResources().getResourceName(itemLayoutRes),
                "AAPT 已生成二进制 XML 与 R.layout 索引");

        // 2. 窗口层
        ViewGroup content = findViewById(android.R.id.content);
        ViewLoadLogger.log(ViewLoadLogger.Trigger.ADAPTER, ViewLoadLogger.Stage.WINDOW,
                "content", "宿主 Activity content=" + content.getClass().getName());

        // 3. 在内容区创建 RecyclerView
        contentContainer.removeAllViews();
        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contentContainer.addView(recyclerView);

        List<String> data = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            data.add((isAlternate ? "B-" : "A-") + "Item " + i);
        }

        LoadDemoAdapter adapter = new LoadDemoAdapter(data, itemLayoutRes);
        recyclerView.setAdapter(adapter);
    }
}
