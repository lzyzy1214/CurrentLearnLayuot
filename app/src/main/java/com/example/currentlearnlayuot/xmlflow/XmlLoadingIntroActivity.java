package com.example.currentlearnlayuot.xmlflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.example.currentlearnlayuot.R;
import com.example.currentlearnlayuot.viewload.ActivityLoadActivity;
import com.example.currentlearnlayuot.viewload.AdapterLoadActivity;
import com.example.currentlearnlayuot.viewload.FragmentLoadActivity;

/**
 * XML 加载流程介绍引导页。
 *
 * 用图文方式展示 Android XML 布局从“文本文件”到“屏幕像素”的完整链路，
 * 并提供三个入口的快捷跳转，让用户能立即验证每一步。
 */
public class XmlLoadingIntroActivity extends AppCompatActivity {

    private NestedScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml_loading_intro);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("XML 加载流程介绍");
            }
        }

        TextView tvFlow = findViewById(R.id.tvFlow);
        if (tvFlow != null) {
            tvFlow.setText(buildFlowText());
        }

        Button btnActivity = findViewById(R.id.btnActivity);
        Button btnFragment = findViewById(R.id.btnFragment);
        Button btnAdapter = findViewById(R.id.btnAdapter);

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

        scrollView = findViewById(R.id.scrollView);
    }

    private String buildFlowText() {
        return "Android XML 布局加载的完整链路：\n\n"
                + "1. 开发者编写 XML 布局文件\n"
                + "   位置：res/layout/xxx.xml\n"
                + "   作用：用声明式语法描述界面结构。\n\n"
                + "2. AAPT2 编译 XML\n"
                + "   命令：aapt2 compile ...\n"
                + "   输出：二进制 XML（.flat 文件）。\n\n"
                + "3. AAPT2 link 生成 resources.arsc 和 R.java\n"
                + "   命令：aapt2 link ...\n"
                + "   输出：APK 内的 resources.arsc 和 R.java。\n\n"
                + "4. APK 打包与安装\n"
                + "   所有资源、代码、清单打包成 APK 并安装到设备。\n\n"
                + "5. Activity 启动与 attach\n"
                + "   ActivityThread 通过 Instrumentation 创建 Activity，\n"
                + "   并调用 attach() 创建 PhoneWindow。\n\n"
                + "6. Activity.setContentView\n"
                + "   把开发者布局设置给 PhoneWindow。\n\n"
                + "7. PhoneWindow 创建 DecorView\n"
                + "   installDecor() 加载系统窗口布局，生成 DecorView。\n\n"
                + "8. 加载系统窗口布局\n"
                + "   找到 android.R.id.content 容器。\n\n"
                + "9. Resources.getLayout(resId)\n"
                + "   用 R.layout.xxx 这个整数 ID 去查资源。\n\n"
                + "10. AssetManager 查 resources.arsc\n"
                + "    把 0x7fxxxxxx 拆成 packageId / typeId / entryId，\n"
                + "    找到二进制 XML 的路径。\n\n"
                + "11. XmlResourceParser 遍历二进制 XML\n"
                + "    把二进制 XML 解析成事件流（START_TAG / END_TAG）。\n\n"
                + "12. LayoutInflater.inflate\n"
                + "    根据标签名反射创建 View。\n\n"
                + "13. Factory2 拦截\n"
                + "    AppCompat 在这里把 TextView 换成 AppCompatTextView。\n\n"
                + "14. 反射创建 View\n"
                + "    Class.forName(...).getConstructor(...).newInstance(...)。\n\n"
                + "15. View 读 TypedArray 属性\n"
                + "    从 AttributeSet 读取 layout_width、text 等属性。\n\n"
                + "16. ViewGroup 递归 addView\n"
                + "    rInflateChildren 递归处理子节点。\n\n"
                + "17. Fragment / Adapter 触发分支\n"
                + "    Fragment.onCreateView、Adapter.onCreateViewHolder 也走 inflate。\n\n"
                + "18. ViewRootImpl.setView 注册窗口\n"
                + "    把 DecorView 注册到 WindowManagerService。\n\n"
                + "19. Choreographer VSYNC 调度\n"
                + "    等待屏幕垂直同步信号。\n\n"
                + "20. performMeasure\n"
                + "    测量每个 View 的宽高。\n\n"
                + "21. performLayout\n"
                + "    确定每个 View 的位置。\n\n"
                + "22. performDraw\n"
                + "    把 View 画到 Canvas。\n\n"
                + "23. Surface.unlockCanvasAndPost\n"
                + "    把图形 buffer 提交给 SurfaceFlinger。\n\n"
                + "24. SurfaceFlinger 合成 → 屏幕\n"
                + "    最终显示到屏幕上。\n\n"
                + "核心结论：\n"
                + "所有 XML 布局加载最终都走 LayoutInflater.inflate(...)。\n"
                + "Activity / Fragment / Adapter 三种入口的区别只在于：\n"
                + "谁持有 LayoutInflater、解析出的 View 挂到哪里、谁来 attach。";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
