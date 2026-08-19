package com.example.currentlearnlayuot.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.currentlearnlayuot.R;
import com.example.currentlearnlayuot.data.ExampleItem;
import com.example.currentlearnlayuot.utils.ExampleInitializer;

/**
 * 单个示例页面：展示标题、说明和示例布局
 */
public class ExampleFragment extends Fragment {

    private static final String ARG_LAYOUT = "layout";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";

    public static ExampleFragment newInstance(ExampleItem item) {
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT, item.layoutResId);
        args.putString(ARG_TITLE, item.title);
        args.putString(ARG_DESC, item.description);
        ExampleFragment fragment = new ExampleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_example, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        String title = args.getString(ARG_TITLE, "");
        String desc = args.getString(ARG_DESC, "");
        @LayoutRes int layoutResId = args.getInt(ARG_LAYOUT, 0);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        FrameLayout container = view.findViewById(R.id.exampleContainer);

        tvTitle.setText(title);
        tvDescription.setText(desc);

        if (layoutResId != 0 && container != null) {
            container.removeAllViews();
            View exampleView = LayoutInflater.from(requireContext())
                    .inflate(layoutResId, container, false);
            container.addView(exampleView);
            ExampleInitializer.init(exampleView, layoutResId);
        }
    }
}
