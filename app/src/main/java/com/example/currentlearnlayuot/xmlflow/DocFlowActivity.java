package com.example.currentlearnlayuot.xmlflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.currentlearnlayuot.R;
import com.example.currentlearnlayuot.viewload.ActivityLoadActivity;
import com.example.currentlearnlayuot.viewload.AdapterLoadActivity;
import com.example.currentlearnlayuot.viewload.FragmentLoadActivity;
import com.example.currentlearnlayuot.viewload.PlainActivityProofActivity;

/**
 * 对应某一个文档版本的 XML 加载流程演示页。
 *
 * 每个文档版本都通过本页面统一展示：
 * - 文档标题、文件名、行数、特点
 * - 三个入口按钮：Activity / Fragment / Adapter
 * - 一个“源码级调用链证明”按钮
 * - 该版本文档强调的关键知识点
 */
public class DocFlowActivity extends AppCompatActivity {

    public static final String EXTRA_DOC_VERSION = "doc_version";

    public static final int DOC_ORIGINAL = 1;
    public static final int DOC_NEW = 2;
    public static final int DOC_NEW_NEW = 3;
    public static final int DOC_INTEGRATED = 4;

    private int docVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_flow);

        docVersion = getIntent().getIntExtra(EXTRA_DOC_VERSION, DOC_ORIGINAL);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(getDocTitle());
            }
        }

        TextView tvDocInfo = findViewById(R.id.tvDocInfo);
        TextView tvHighlights = findViewById(R.id.tvHighlights);
        TextView tvProof = findViewById(R.id.tvProof);

        if (tvDocInfo != null) tvDocInfo.setText(buildDocInfo());
        if (tvHighlights != null) tvHighlights.setText(buildHighlights());
        if (tvProof != null) tvProof.setText(buildProofText());

        Button btnActivity = findViewById(R.id.btnActivity);
        Button btnFragment = findViewById(R.id.btnFragment);
        Button btnAdapter = findViewById(R.id.btnAdapter);
        Button btnPlainProof = findViewById(R.id.btnPlainProof);

        if (btnActivity != null) {
            btnActivity.setOnClickListener(v ->
                    startActivity(new Intent(this, ActivityLoadActivity.class)));
        }
        if (btnFragment != null) {
            btnFragment.setOnClickListener(v ->
                    startActivity(new Intent(this, FragmentLoadActivity.class)));
        }
        if (btnAdapter != null) {
            btnAdapter.setOnClickListener(v ->
                    startActivity(new Intent(this, AdapterLoadActivity.class)));
        }
        if (btnPlainProof != null) {
            btnPlainProof.setOnClickListener(v ->
                    startActivity(new Intent(this, PlainActivityProofActivity.class)));
        }
    }

    private String getDocTitle() {
        switch (docVersion) {
            case DOC_ORIGINAL:
                return "① 原版 24 步文档演示";
            case DOC_NEW:
                return "② 扩展版文档演示";
            case DOC_NEW_NEW:
                return "③ 超详细 new-new 文档演示";
            case DOC_INTEGRATED:
                return "④ 整合版文档演示";
            default:
                return "文档演示";
        }
    }

    private String buildDocInfo() {
        switch (docVersion) {
            case DOC_ORIGINAL:
                return "文档：20260802_xml_loading_flow.md\n"
                        + "规模：约 3000+ 行\n"
                        + "定位：Android XML 加载流程的 24 步主线拆解，适合建立整体认知。";
            case DOC_NEW:
                return "文档：20260802-new_xml_loading_flow.md\n"
                        + "规模：约 4000+ 行\n"
                        + "定位：在原版基础上补充更多源码细节和 Android Studio 工具操作。";
            case DOC_NEW_NEW:
                return "文档：20260802-new-new-xml_loading_flow.md\n"
                        + "规模：约 11000+ 行，41 个部分\n"
                        + "定位：超详细版，AOSP 源码逐行导读，从 AssetManager 到 SurfaceFlinger。";
            case DOC_INTEGRATED:
                return "文档：20260802-new-new-整合版-xml_loading_flow.md\n"
                        + "规模：约 8000+ 行\n"
                        + "定位：按时间线重新组织，把资源 ID、resources.arsc、二进制 XML、AOSP 源码、工具操作嵌入对应阶段。";
            default:
                return "";
        }
    }

    private String buildHighlights() {
        switch (docVersion) {
            case DOC_ORIGINAL:
                return "核心关注点：\n"
                        + "• 24 步完整链路，从 XML 文本到屏幕像素\n"
                        + "• Activity / Fragment / Adapter 三种触发入口对比\n"
                        + "• LayoutInflater.inflate 是 XML 加载的心脏\n"
                        + "• Window → PhoneWindow → DecorView → content 的层级关系";
            case DOC_NEW:
                return "核心关注点：\n"
                        + "• 扩展了每个步骤的细节和源码引用\n"
                        + "• 增加 Android Studio 工具实战（APK Analyzer、Layout Inspector 等）\n"
                        + "• 补充 resources.arsc 和二进制 XML 结构说明\n"
                        + "• 更多可复现代码和命令行示例";
            case DOC_NEW_NEW:
                return "核心关注点：\n"
                        + "• AOSP 源码完全导读：AssetManager2.cpp、XmlBlock.cpp、LayoutInflater.java、ViewRootImpl.java\n"
                        + "• Android Studio 按钮级操作手册：System Trace、CPU Profiler、Memory Profiler 等\n"
                        + "• 0x7F 资源映射超深度拆解：packageId / typeId / entryId\n"
                        + "• 二进制 XML 与 resources.arsc 逐字节实测";
            case DOC_INTEGRATED:
                return "核心关注点：\n"
                        + "• 按前→后时间线重新组织，阅读更顺畅\n"
                        + "• 资源 ID、resources.arsc、二进制 XML 直接嵌入 AAPT2/资源查找阶段\n"
                        + "• AOSP 源码和工具操作放在对应流程节点\n"
                        + "• 保留全部深度，同时降低跳跃感";
            default:
                return "";
        }
    }

    private String buildProofText() {
        return "点击下方按钮，分别复现文档中描述的三种触发入口。\n\n"
                + "每个演示页都会：\n"
                + "1. 用统一格式日志输出当前阶段（Compile/Window/Inflate/Factory2/Attach/Measure/Layout/Draw）；\n"
                + "2. 用阶段指示器高亮当前执行到的阶段；\n"
                + "3. 在关键节点抓取真实调用栈，证明不是手写日志；\n"
                + "4. 可切换布局，反复观察完整流程。\n\n"
                + "【源码级调用链证明】会用 Debug.startMethodTracing 录制 setContentView 的完整方法调用链，\n"
                + "生成 .trace 文件，可用 Android Studio Profiler 打开查看 PhoneWindow → LayoutInflater → View.measure/layout/draw。";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
