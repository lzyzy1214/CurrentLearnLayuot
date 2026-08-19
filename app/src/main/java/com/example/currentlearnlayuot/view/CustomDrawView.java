package com.example.currentlearnlayuot.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 自定义 View，演示 measure/layout/draw 中的 onDraw 阶段
 */
public class CustomDrawView extends View {

    private final Paint paint;
    private final RectF rectF;

    public CustomDrawView(Context context) {
        this(context, null);
    }

    public CustomDrawView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize(400, widthMeasureSpec);
        int height = resolveSize(400, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int cx = width / 2;
        int cy = height / 2;
        int radius = Math.min(width, height) / 3;

        // 绘制背景圆
        paint.setColor(Color.parseColor("#2196F3"));
        canvas.drawCircle(cx, cy, radius, paint);

        // 绘制内圆
        paint.setColor(Color.parseColor("#90CAF9"));
        canvas.drawCircle(cx, cy, radius * 0.7f, paint);

        // 绘制矩形
        rectF.set(cx - radius * 0.5f, cy - radius * 0.5f,
                cx + radius * 0.5f, cy + radius * 0.5f);
        paint.setColor(Color.parseColor("#0D47A1"));
        canvas.drawRoundRect(rectF, 16, 16, paint);

        // 绘制文字
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("onDraw", cx, cy + 12, paint);
    }
}
