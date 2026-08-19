package com.example.currentlearnlayuot.viewload;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

/**
 * LayoutInflater.Factory2 示例：在 inflate 过程中拦截每个标签名，
 * 演示 Factory2 如何监听/替换 View 的创建。
 *
 * 本实现仅记录日志并返回 null，让系统继续走默认的反射创建流程。
 */
public class InflateLoggerFactory implements LayoutInflater.Factory2 {

    private final ViewLoadLogger.Trigger trigger;

    public InflateLoggerFactory(ViewLoadLogger.Trigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        String parentName = parent == null ? "null" : parent.getClass().getSimpleName();
        ViewLoadLogger.log(trigger, ViewLoadLogger.Stage.FACTORY, name,
                "Factory2 拦截 tag=" + name + ", parent=" + parentName);

        // 抓取真实调用栈，证明是谁调用了 LayoutInflater.inflate / createViewFromTag
        String stack = StackTraceUtil.capture();
        ViewLoadLogger.logStack(trigger.name() + " 入口的 LayoutInflater 调用栈", stack);
        return null;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }
}
