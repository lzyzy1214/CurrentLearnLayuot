package com.example.currentlearnlayuot.viewload;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 调用栈抓取与过滤工具。
 * 用于在关键节点（inflate、measure、layout、draw）抓取当前线程调用栈，
 * 并过滤出 Android 框架层和本项目的帧，以证明真实的调用链路。
 */
public class StackTraceUtil {

    // 默认只保留这些包名下的栈帧，避免被系统/Looper/Handler 等噪音淹没
    private static final String[] DEFAULT_PREFIXES = {
            "android.app.",
            "com.android.internal.policy.",
            "android.view.",
            "android.widget.",
            "androidx.",
            "com.example.currentlearnlayuot."
    };

    /**
     * 抓取当前线程调用栈，并过滤出关键帧。
     *
     * @param extraPrefixes 额外需要保留的包名前缀
     */
    public static String capture(String... extraPrefixes) {
        Set<String> prefixes = new HashSet<>(Arrays.asList(DEFAULT_PREFIXES));
        prefixes.addAll(Arrays.asList(extraPrefixes));

        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (StackTraceElement e : stack) {
            String className = e.getClassName();
            if (shouldKeep(className, prefixes)) {
                sb.append("    at ")
                        .append(className).append(".")
                        .append(e.getMethodName()).append("(")
                        .append(e.getFileName()).append(":")
                        .append(e.getLineNumber()).append(")\n");
                count++;
                if (count >= 40) {
                    sb.append("    ... (已截断，完整栈请用 Debug.startMethodTracing 录制)\n");
                    break;
                }
            }
        }
        return sb.toString().trim();
    }

    private static boolean shouldKeep(String className, Set<String> prefixes) {
        for (String prefix : prefixes) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
