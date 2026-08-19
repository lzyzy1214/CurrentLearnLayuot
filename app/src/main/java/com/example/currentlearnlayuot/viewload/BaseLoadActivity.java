package com.example.currentlearnlayuot.viewload;

import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.example.currentlearnlayuot.R;

/**
 * 三种触发方式的公共基类。
 * 提供统一的工具栏、阶段指示器、日志区和切换布局按钮。
 */
public abstract class BaseLoadActivity extends AppCompatActivity {

    protected StageIndicator stageIndicator;
    protected TextView tvProcessDesc;
    protected TextView tvLog;
    protected NestedScrollView logScroll;
    protected FrameLayout contentContainer;
    protected Button btnSwitch;
    protected Button btnShowStack;

    protected boolean isAlternate = false;
    protected final StringBuilder stackTraceBuffer = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_base);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(getPageTitle());
            }
        }

        stageIndicator = findViewById(R.id.stageIndicator);
        tvProcessDesc = findViewById(R.id.tvProcessDesc);
        tvLog = findViewById(R.id.tvLog);
        logScroll = findViewById(R.id.logScroll);
        contentContainer = findViewById(R.id.contentContainer);
        btnSwitch = findViewById(R.id.btnSwitch);
        btnShowStack = findViewById(R.id.btnShowStack);

        // 显示当前触发方式的完整加载过程说明
        tvProcessDesc.setText(getProcessDescription());

        // 绑定统一日志回调：日志同时输出到 Logcat 和本页 TextView
        ViewLoadLogger.setUiCallback(this::appendLog);
        ViewLoadLogger.setStageListener(stageIndicator);
        ViewLoadLogger.setStackCallback(this::appendStack);

        btnSwitch.setOnClickListener(v -> {
            isAlternate = !isAlternate;
            clearLog();
            stackTraceBuffer.setLength(0);
            stageIndicator.reset();
            runLoadFlow();
        });

        btnShowStack.setOnClickListener(v -> showStackDialog());

        // 初始触发一次完整流程
        clearLog();
        stackTraceBuffer.setLength(0);
        stageIndicator.reset();
        runLoadFlow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 防止静态回调泄漏
        ViewLoadLogger.setUiCallback(null);
        ViewLoadLogger.setStageListener(null);
        ViewLoadLogger.setStackCallback(null);
    }

    /**
     * 子类返回页面标题。
     */
    protected abstract String getPageTitle();

    /**
     * 子类返回本触发方式的完整加载过程说明（显示在“加载过程”卡片中）。
     */
    protected abstract String getProcessDescription();

    /**
     * 子类实现具体的加载流程：切换布局时会被再次调用。
     */
    protected abstract void runLoadFlow();

    protected void appendLog(String log) {
        tvLog.append(log + "\n");
        // 自动滚动到底部
        logScroll.post(() -> logScroll.fullScroll(NestedScrollView.FOCUS_DOWN));
    }

    protected void clearLog() {
        tvLog.setText("");
    }

    protected void appendStack(String title, String stack) {
        stackTraceBuffer.append("===== ").append(title).append(" =====\n")
                .append(stack).append("\n\n");
    }

    protected void showStackDialog() {
        String content = stackTraceBuffer.length() > 0
                ? stackTraceBuffer.toString()
                : "暂无调用栈，请先加载布局或执行渲染。";

        TextView textView = new TextView(this);
        textView.setText(content);
        textView.setTextIsSelectable(true);
        textView.setPadding(dp(16), dp(16), dp(16), dp(16));
        textView.setTextSize(11f);
        textView.setTextColor(0xFFA5D6A7);
        textView.setBackgroundColor(0xFF1B1B1B);

        NestedScrollView scrollView = new NestedScrollView(this);
        scrollView.addView(textView);
        scrollView.setBackgroundColor(0xFF1B1B1B);

        new AlertDialog.Builder(this)
                .setTitle("关键调用栈（源码级证明）")
                .setView(scrollView)
                .setPositiveButton("关闭", null)
                .show();
    }

    private int dp(int px) {
        return (int) (px * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
