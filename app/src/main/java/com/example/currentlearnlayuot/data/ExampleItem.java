package com.example.currentlearnlayuot.data;

import androidx.annotation.LayoutRes;

/**
 * 单个示例项数据模型
 */
public class ExampleItem {
    public final String title;
    public final String description;
    @LayoutRes
    public final int layoutResId;

    public ExampleItem(String title, String description, @LayoutRes int layoutResId) {
        this.title = title;
        this.description = description;
        this.layoutResId = layoutResId;
    }
}
