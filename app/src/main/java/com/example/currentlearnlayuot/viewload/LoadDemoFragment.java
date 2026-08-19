package com.example.currentlearnlayuot.viewload;

import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment 触发方式演示用 Fragment。
 *
 * 在 onCreateView 中：
 * 1. 使用带 Factory2 的 LayoutInflater 加载布局。
 * 2. 返回 View 后通过 view.post() 在 attach 完成时打印父链，验证最终挂到 Activity 的 content。
 */
public class LoadDemoFragment extends Fragment {

    private final int layoutRes;
    private final ViewLoadLogger.Trigger trigger;

    public LoadDemoFragment(int layoutRes, ViewLoadLogger.Trigger trigger) {
        this.layoutRes = layoutRes;
        this.trigger = trigger;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewLoadLogger.log(trigger, ViewLoadLogger.Stage.INFLATE,
                requireContext().getResources().getResourceName(layoutRes),
                "Fragment.onCreateView 开始 inflate");

        // 使用 ContextThemeWrapper 获取没有 AppCompat Factory 的 LayoutInflater，
        // 从而能安全设置我们自己的 Factory2。
        ContextThemeWrapper wrapper = new ContextThemeWrapper(requireContext(), requireActivity().getTheme());
        LayoutInflater themedInflater = LayoutInflater.from(wrapper);
        themedInflater.setFactory2(new InflateLoggerFactory(trigger));

        View view = themedInflater.inflate(layoutRes, container, false);

        view.post(() -> {
            // 此时 Fragment 的 View 已经被 FragmentManager attach 到 Activity 的 content 容器
            StringBuilder parentChain = new StringBuilder();
            View current = view;
            while (current != null) {
                parentChain.append(current.getClass().getSimpleName());
                ViewParent parent = current.getParent();
                if (parent instanceof View) {
                    parentChain.append(" -> ");
                    current = (View) parent;
                } else {
                    break;
                }
            }
            ViewLoadLogger.log(trigger, ViewLoadLogger.Stage.ATTACH,
                    view.getClass().getSimpleName(), "父链：" + parentChain);
        });

        return view;
    }
}
