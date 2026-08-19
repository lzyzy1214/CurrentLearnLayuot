package com.example.currentlearnlayuot.xmlflow;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.currentlearnlayuot.R;
import com.google.android.material.card.MaterialCardView;

/**
 * XML 加载流程文档演示总入口。
 *
 * 对应 docs/ 目录下的四个文档版本：
 * 1. 20260802_xml_loading_flow.md（原版 24 步）
 * 2. 20260802-new_xml_loading_flow.md（扩展版）
 * 3. 20260802-new-new-xml_loading_flow.md（超详细 new-new 版）
 * 4. 20260802-new-new-整合版-xml_loading_flow.md（整合版）
 *
 * 每个版本都有 Activity / Fragment / Adapter 三个入口的演示，
 * 并通过一个统一页面证明和复现文档中描述的 XML 加载流程。
 */
public class XmlLoadingFlowHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml_loading_flow_hub);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("XML 加载流程文档演示");
            }
        }

        // 1. XML 加载流程介绍引导页
        bindCard(R.id.cardIntro, "XML 加载流程介绍",
                "从零开始看懂 Android XML 布局是怎么变成屏幕像素的。",
                v -> startActivity(new Intent(this, XmlLoadingIntroActivity.class)));

        // 2. 原版 24 步文档
        bindCard(R.id.cardDocOriginal, "① 原版 24 步文档",
                "《Android XML 加载流程：逐步拆解 + 源码级说明》\n约 3000+ 行，24 步主线。",
                v -> launchDocFlow(DocFlowActivity.DOC_ORIGINAL));

        // 3. 扩展版文档
        bindCard(R.id.cardDocNew, "② 扩展版文档",
                "《Android XML 加载流程：扩展说明》\n在原版基础上补充更多细节与工具。",
                v -> launchDocFlow(DocFlowActivity.DOC_NEW));

        // 4. 超详细 new-new 版
        bindCard(R.id.cardDocNewNew, "③ 超详细 new-new 文档",
                "《Android XML 加载流程：new-new 超详细版》\n约 11000+ 行，41 个部分，AOSP 源码逐行解读。",
                v -> launchDocFlow(DocFlowActivity.DOC_NEW_NEW));

        // 5. 整合版文档
        bindCard(R.id.cardDocIntegrated, "④ 整合版文档",
                "《Android XML 加载流程：整合版》\n按时间线重新组织，把资源 ID / resources.arsc / 工具嵌入对应阶段。",
                v -> launchDocFlow(DocFlowActivity.DOC_INTEGRATED));
    }

    private void bindCard(int cardId, String title, String desc,
                          android.view.View.OnClickListener listener) {
        MaterialCardView card = findViewById(cardId);
        if (card == null) return;

        androidx.appcompat.widget.AppCompatTextView tvTitle = card.findViewById(R.id.tvCardTitle);
        androidx.appcompat.widget.AppCompatTextView tvDesc = card.findViewById(R.id.tvCardDesc);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvDesc != null) tvDesc.setText(desc);
        card.setOnClickListener(listener);
    }

    private void launchDocFlow(int docVersion) {
        Intent intent = new Intent(this, DocFlowActivity.class);
        intent.putExtra(DocFlowActivity.EXTRA_DOC_VERSION, docVersion);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
