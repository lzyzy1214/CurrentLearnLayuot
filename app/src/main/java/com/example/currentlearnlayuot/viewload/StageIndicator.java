package com.example.currentlearnlayuot.viewload;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.EnumMap;
import java.util.Map;

/**
 * 阶段指示器：用一排 Chip 样式标签高亮当前视图加载阶段。
 * 阶段顺序与流程图一致：Compile → Window → Inflate → Factory2 → Attach → Measure → Layout → Draw。
 */
public class StageIndicator extends LinearLayout implements ViewLoadLogger.StageListener {

    private final Map<ViewLoadLogger.Stage, TextView> chipMap = new EnumMap<>(ViewLoadLogger.Stage.class);

    private static final int COLOR_ACTIVE = 0xFF4CAF50;
    private static final int COLOR_INACTIVE = 0xFFE0E0E0;
    private static final int TEXT_ACTIVE = 0xFFFFFFFF;
    private static final int TEXT_INACTIVE = 0xFF757575;

    public StageIndicator(Context context) {
        super(context);
        init();
    }

    public StageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.HORIZONTAL);
        setPadding(dp(8), dp(8), dp(8), dp(8));
        for (ViewLoadLogger.Stage stage : ViewLoadLogger.Stage.values()) {
            TextView chip = new TextView(getContext());
            chip.setText(stage.label);
            chip.setTextSize(11f);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(8), dp(4), dp(8), dp(4));

            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(dp(4), 0, dp(4), 0);
            chip.setLayoutParams(params);

            updateChip(chip, false);
            addView(chip);
            chipMap.put(stage, chip);
        }
    }

    @Override
    public void onStageActive(ViewLoadLogger.Stage stage) {
        post(() -> {
            TextView chip = chipMap.get(stage);
            if (chip != null) {
                updateChip(chip, true);
            }
        });
    }

    /**
     * 重置所有阶段为未激活状态。
     */
    public void reset() {
        post(() -> {
            for (TextView chip : chipMap.values()) {
                updateChip(chip, false);
            }
        });
    }

    private void updateChip(TextView chip, boolean active) {
        chip.setBackgroundColor(active ? COLOR_ACTIVE : COLOR_INACTIVE);
        chip.setTextColor(active ? TEXT_ACTIVE : TEXT_INACTIVE);
    }

    private int dp(int px) {
        return (int) (px * getResources().getDisplayMetrics().density + 0.5f);
    }
}
