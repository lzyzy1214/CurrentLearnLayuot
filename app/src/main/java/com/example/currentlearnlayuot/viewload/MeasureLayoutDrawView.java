package com.example.currentlearnlayuot.viewload;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义 View，用于演示 Measure / Layout / Draw 三个渲染阶段。
 * 每个阶段都会输出统一格式日志，并在 onDraw 中绘制当前阶段标记。
 */
public class MeasureLayoutDrawView extends View {

    private final Paint paint;
    private boolean measuredOnce = false;
    private boolean layoutOnce = false;
    private boolean drawnOnce = false;

    public MeasureLayoutDrawView(Context context) {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public MeasureLayoutDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public MeasureLayoutDrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 演示 Measure 阶段：解析 MeasureSpec 并决定自身尺寸
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!measuredOnce) {
            measuredOnce = true;
            ViewLoadLogger.log(ViewLoadLogger.Trigger.RENDER, ViewLoadLogger.Stage.MEASURE, this,
                    "onMeasure spec=" + MeasureSpec.toString(widthMeasureSpec)
                            + "x" + MeasureSpec.toString(heightMeasureSpec));
            // 抓取真实调用栈，证明 ViewRootImpl.performTraversals -> measure
            ViewLoadLogger.logStack("Measure 阶段真实调用栈", StackTraceUtil.capture());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // 演示 Layout 阶段：确定自身在父容器中的位置
        super.onLayout(changed, left, top, right, bottom);
        if (!layoutOnce) {
            layoutOnce = true;
            ViewLoadLogger.log(ViewLoadLogger.Trigger.RENDER, ViewLoadLogger.Stage.LAYOUT, this,
                    "onLayout changed=" + changed + " bounds=[" + left + "," + top + "," + right + "," + bottom + "]");
            ViewLoadLogger.logStack("Layout 阶段真实调用栈", StackTraceUtil.capture());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 为避免每帧刷屏，只记录第一次 Draw
        if (!drawnOnce) {
            drawnOnce = true;
            ViewLoadLogger.log(ViewLoadLogger.Trigger.RENDER, ViewLoadLogger.Stage.DRAW, this,
                    "onDraw canvas=" + canvas);
            ViewLoadLogger.logStack("Draw 阶段真实调用栈", StackTraceUtil.capture());
        }

        // 绘制一个带阶段说明的色块
        int w = getWidth();
        int h = getHeight();
        paint.setColor(0xFFB2DFDB);
        canvas.drawRect(0, 0, w, h, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(36f);
        String text = "Render View";
        float textWidth = paint.measureText(text);
        canvas.drawText(text, (w - textWidth) / 2f, h / 2f, paint);
    }
}
