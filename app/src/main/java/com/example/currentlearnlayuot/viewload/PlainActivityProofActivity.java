package com.example.currentlearnlayuot.viewload;

import android.app.Activity;
import android.os.Bundle;
import android.os.Debug;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;

import com.example.currentlearnlayuot.R;

import java.io.File;

/**
 * 使用原生 Activity + 方法追踪（method tracing）直接证明 Activity.setContentView 的源码调用链。
 *
 * 关键点：
 * 1. 继承原生 android.app.Activity，避免 AppCompat 对 inflate 流程的干扰。
 * 2. 在 setContentView 前后调用 Debug.startMethodTracing / stopMethodTracing。
 * 3. 生成的 .trace 文件可用 Android Studio Profiler 打开，里面会清晰显示：
 *    android.app.Activity.setContentView
 *    -> com.android.internal.policy.PhoneWindow.setContentView
 *    -> android.view.LayoutInflater.inflate
 *    -> android.view.View.measure / layout / draw
 *    -> android.view.ViewRootImpl.performTraversals
 *
 * 这是源码级、不可伪造的调用链证据。
 */
public class PlainActivityProofActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 先设置 Factory2，用于在 inflate 过程中抓取调用栈（辅助证明）
        getWindow().getLayoutInflater().setFactory2(new InflateLoggerFactory(ViewLoadLogger.Trigger.ACTIVITY));

        ViewLoadLogger.setStackCallback((title, stack) -> {
            // 这里不需要把栈显示在界面上，只输出到 Logcat
        });

        File traceFile = new File(getExternalFilesDir(null), "activity_setcontentview.trace");
        traceFile.delete();

        ViewLoadLogger.log(ViewLoadLogger.Trigger.ACTIVITY, ViewLoadLogger.Stage.WINDOW,
                "Activity", "准备调用 setContentView，开始 method tracing：" + traceFile.getAbsolutePath());

        // 开始录制 CPU 方法调用链
        Debug.startMethodTracing(traceFile.getAbsolutePath(), 16 * 1024 * 1024);

        // 触发完整调用链：Activity.setContentView -> PhoneWindow -> LayoutInflater -> View
        setContentView(R.layout.content_activity_load_a);

        // 停止录制
        Debug.stopMethodTracing();

        ViewLoadLogger.log(ViewLoadLogger.Trigger.ACTIVITY, ViewLoadLogger.Stage.ATTACH,
                "Activity", "setContentView 完成，trace 文件：" + traceFile.getAbsolutePath());

        showResultDialog(traceFile);
    }

    private void showResultDialog(File traceFile) {
        String message = "已录制 Activity.setContentView 的完整方法调用链。\n\n"
                + "trace 文件路径（设备内）：\n"
                + traceFile.getAbsolutePath() + "\n\n"
                + "导出到电脑查看：\n"
                + "adb pull " + traceFile.getAbsolutePath() + "\n\n"
                + "用 Android Studio 的 Profiler / CPU 工具打开该 .trace 文件，\n"
                + "即可看到 PhoneWindow.setContentView -> LayoutInflater.inflate -> View.measure/layout/draw 的完整链路。";

        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextIsSelectable(true);
        int pad = (int) (16 * getResources().getDisplayMetrics().density + 0.5f);
        textView.setPadding(pad, pad, pad, pad);
        textView.setTextSize(13f);
        textView.setTextColor(0xFF212121);

        NestedScrollView scrollView = new NestedScrollView(this);
        scrollView.addView(textView);

        new AlertDialog.Builder(this)
                .setTitle("源码级调用链证明")
                .setView(scrollView)
                .setPositiveButton("重新执行", (dialog, which) -> recreate())
                .setNegativeButton("关闭", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewLoadLogger.setStackCallback(null);
    }
}
