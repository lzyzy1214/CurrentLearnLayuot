package com.example.currentlearnlayuot.data;

import java.util.Collections;
import java.util.List;

/**
 * 章节数据模型
 */
public class Chapter {
    public final int id;
    public final String title;
    public final String summary;
    public final String difficulty;
    public final List<ExampleItem> examples;

    public Chapter(int id, String title, String summary, String difficulty, List<ExampleItem> examples) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.difficulty = difficulty;
        this.examples = examples == null ? Collections.emptyList() : examples;
    }
}
