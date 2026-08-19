package com.example.currentlearnlayuot.viewload;

import android.view.ViewGroup;

import androidx.fragment.app.FragmentTransaction;

import com.example.currentlearnlayuot.R;

/**
 * Fragment 触发方式演示页。
 *
 * 流程：
 * 1. Activity 的 content 容器作为 Fragment 的宿主。
 * 2. 通过 FragmentTransaction.replace 触发 Fragment.onCreateView。
 * 3. Fragment 内部使用 LayoutInflater + Factory2 inflate 布局。
 * 4. Fragment 的 View 最终 attach 到 Activity content 下。
 */
public class FragmentLoadActivity extends BaseLoadActivity {

    @Override
    protected String getPageTitle() {
        return "Fragment.onCreateView";
    }

    @Override
    protected String getProcessDescription() {
        return "1. FragmentManager 执行 replace / commit\n" +
                "2. 触发 Fragment.onCreateView(LayoutInflater, container, savedInstanceState)\n" +
                "3. LayoutInflater 解析布局，Factory2 拦截每个标签\n" +
                "4. 返回 View 后，FragmentManager 把 View attach 到 Activity 的 content\n" +
                "5. 父链：FragmentView → FragmentContainerView → content → DecorView\n" +
                "6. 进入 Measure → Layout → Draw 渲染到屏幕";
    }

    @Override
    protected void runLoadFlow() {
        int layoutRes = isAlternate ? R.layout.content_fragment_load_b : R.layout.content_fragment_load_a;

        // 1. 编译期
        ViewLoadLogger.log(ViewLoadLogger.Trigger.FRAGMENT, ViewLoadLogger.Stage.COMPILE,
                getResources().getResourceName(layoutRes),
                "AAPT 已生成二进制 XML 与 R.layout 索引");

        // 2. 窗口层：展示 Activity 的 content 容器
        ViewGroup content = findViewById(android.R.id.content);
        ViewLoadLogger.log(ViewLoadLogger.Trigger.FRAGMENT, ViewLoadLogger.Stage.WINDOW,
                "content", "宿主 Activity content=" + content.getClass().getName());

        // 3. 触发 Fragment 加载流程
        LoadDemoFragment fragment = new LoadDemoFragment(layoutRes, ViewLoadLogger.Trigger.FRAGMENT);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(contentContainer.getId(), fragment);
        ft.commitNow();
    }
}
