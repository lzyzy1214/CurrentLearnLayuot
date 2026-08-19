package com.example.currentlearnlayuot.viewload;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.currentlearnlayuot.R;

/**
 * Activity 触发方式演示页。
 *
 * 流程：
 * 1. Activity.setContentView 设置根布局。
 * 2. 通过 Window / PhoneWindow / DecorView 找到 content 容器。
 * 3. 使用带 Factory2 的 LayoutInflater 重新 inflate 内容布局，展示拦截过程。
 * 4. 内容 View 被 attach 到 Activity 的 content 容器。
 * 5. 内容中的 MeasureLayoutDrawView 进入 Measure / Layout / Draw。
 */
public class ActivityLoadActivity extends BaseLoadActivity {

    @Override
    protected String getPageTitle() {
        return "Activity.setContentView";
    }

    @Override
    protected String getProcessDescription() {
        return "1. Activity 调用 setContentView(R.layout.xxx)\n" +
                "2. 进入 Window → PhoneWindow → DecorView\n" +
                "3. 找到 content 容器（android.R.id.content）\n" +
                "4. LayoutInflater 解析二进制 XML，Factory2 拦截每个标签\n" +
                "5. 生成 View/ViewGroup 树并 attach 到 content\n" +
                "6. 进入 Measure → Layout → Draw 渲染到屏幕";
    }

    @Override
    protected void runLoadFlow() {
        int layoutRes = isAlternate ? R.layout.content_activity_load_b : R.layout.content_activity_load_a;

        // 1. 模拟编译期：R.layout.xxx 是 AAPT 生成的资源索引
        ViewLoadLogger.log(ViewLoadLogger.Trigger.ACTIVITY, ViewLoadLogger.Stage.COMPILE,
                getResources().getResourceName(layoutRes),
                "AAPT 已生成二进制 XML 与 R.layout 索引");

        // 2. 窗口层：Activity 持有 Window，Window 持有 DecorView，DecorView 里有 content
        ViewGroup content = findViewById(android.R.id.content);
        ViewLoadLogger.log(ViewLoadLogger.Trigger.ACTIVITY, ViewLoadLogger.Stage.WINDOW,
                "content", "Window 容器类型=" + content.getClass().getName()
                        + ", 子 View 数=" + content.getChildCount());

        // 3. 解析层：使用全新的 ContextThemeWrapper 获取 LayoutInflater，避免与 AppCompat Factory 冲突
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, getTheme());
        LayoutInflater inflater = LayoutInflater.from(wrapper);
        // 设置 Factory2 监听每一个 XML 标签的创建
        inflater.setFactory2(new InflateLoggerFactory(ViewLoadLogger.Trigger.ACTIVITY));

        // 4. inflate 内容布局并添加到 contentContainer
        View contentView = inflater.inflate(layoutRes, contentContainer, false);
        ViewLoadLogger.log(ViewLoadLogger.Trigger.ACTIVITY, ViewLoadLogger.Stage.INFLATE,
                contentView.getClass().getSimpleName(), "LayoutInflater 完成 inflate");

        contentContainer.removeAllViews();
        contentContainer.addView(contentView);

        ViewLoadLogger.log(ViewLoadLogger.Trigger.ACTIVITY, ViewLoadLogger.Stage.ATTACH,
                contentView.getClass().getSimpleName(),
                "已 attach 到 parent=" + contentContainer.getClass().getSimpleName());
    }
}
