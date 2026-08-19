package com.example.currentlearnlayuot.viewload;

import android.util.Log;
import android.view.View;

/**
 * 视图加载流程统一日志器。
 * 所有触发方式（Activity / Fragment / Adapter）以及 LayoutInflater、Factory2、
 * Measure / Layout / Draw 等关键节点都通过本类输出，保证 Logcat 与界面日志格式一致。
 */
public class ViewLoadLogger {

    public static final String TAG = "ViewLoadFlow";

    /**
     * 为每种触发方式生成独立 Tag，方便 Logcat 按入口过滤。
     * 例如：ViewLoadFlow_ACTIVITY、ViewLoadFlow_FRAGMENT、ViewLoadFlow_ADAPTER。
     */
    private static String makeTag(Trigger trigger) {
        return TAG + "_" + trigger.name();
    }

    /** 触发方式 */
    public enum Trigger {
        ACTIVITY,
        FRAGMENT,
        ADAPTER,
        FACTORY2,
        RENDER
    }

    /** 流程阶段 */
    public enum Stage {
        COMPILE("Compile"),
        WINDOW("Window"),
        INFLATE("Inflate"),
        FACTORY("Factory2"),
        ATTACH("Attach"),
        MEASURE("Measure"),
        LAYOUT("Layout"),
        DRAW("Draw");

        public final String label;

        Stage(String label) {
            this.label = label;
        }
    }

    public interface UiCallback {
        void onLog(String log);
    }

    public interface StageListener {
        void onStageActive(Stage stage);
    }

    public interface StackCallback {
        void onStack(String title, String stack);
    }

    private static UiCallback sUiCallback;
    private static StageListener sStageListener;
    private static StackCallback sStackCallback;

    public static void setUiCallback(UiCallback callback) {
        sUiCallback = callback;
    }

    public static void setStageListener(StageListener listener) {
        sStageListener = listener;
    }

    public static void setStackCallback(StackCallback callback) {
        sStackCallback = callback;
    }

    /**
     * 输出一段调用栈到 Logcat，并同步到界面。
     */
    public static void logStack(String title, String stack) {
        String tag = TAG + "_STACK";
        Log.i(tag, "===== " + title + " =====");
        Log.i(tag, stack);
        if (sStackCallback != null) {
            sStackCallback.onStack(title, stack);
        }
    }

    /**
     * 输出一条统一格式日志。
     *
     * @param trigger 触发方式
     * @param stage   当前阶段
     * @param viewId  视图标识，可为类名或资源名
     * @param message 阶段描述
     */
    public static void log(Trigger trigger, Stage stage, String viewId, String message) {
        String line = String.format("[%s][%s] %s (id=%s)",
                trigger.name(), stage.label, message, viewId);
        // 每种触发方式使用独立 Tag，方便单独过滤
        Log.i(makeTag(trigger), line);
        notifyUi(line);
        notifyStage(stage);
    }

    /**
     * 以 View 实例作为视图标识输出日志。
     */
    public static void log(Trigger trigger, Stage stage, View view, String message) {
        String id = view == null ? "null" : view.getClass().getSimpleName() + "@" + Integer.toHexString(view.hashCode());
        log(trigger, stage, id, message);
    }

    private static void notifyUi(String line) {
        if (sUiCallback != null) {
            sUiCallback.onLog(line);
        }
    }

    private static void notifyStage(Stage stage) {
        if (sStageListener != null) {
            sStageListener.onStageActive(stage);
        }
    }
}
