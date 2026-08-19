package com.example.currentlearnlayuot.utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.ViewStub;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.currentlearnlayuot.R;
import com.example.currentlearnlayuot.adapter.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 示例布局初始化器，为需要动态逻辑的示例设置数据和交互
 */
public class ExampleInitializer {

    private ExampleInitializer() {
    }

    public static void init(View rootView, int layoutResId) {
        if (rootView == null) {
            return;
        }
        if (layoutResId == R.layout.example_03_frame_loading) {
            initFrameLoading(rootView);
        } else if (layoutResId == R.layout.example_03_frame_animation) {
            initFrameAnimation(rootView);
        } else if (layoutResId == R.layout.example_05_nested_scroll) {
            initNestedScroll(rootView);
        } else if (layoutResId == R.layout.example_06_recycler_linear) {
            initRecyclerLinear(rootView);
        } else if (layoutResId == R.layout.example_06_recycler_grid) {
            initRecyclerGrid(rootView);
        } else if (layoutResId == R.layout.example_06_recycler_staggered) {
            initRecyclerStaggered(rootView);
        } else if (layoutResId == R.layout.example_07_viewstub) {
            initViewStub(rootView);
        } else if (layoutResId == R.layout.example_09_recycler) {
            initComposeRecycler(rootView);
        } else if (layoutResId == R.layout.example_09_state) {
            initState(rootView);
        } else if (layoutResId == R.layout.example_09_animation) {
            initAnimation(rootView);
        }
    }

    private static void initFrameLoading(View root) {
        Button btn = root.findViewById(R.id.btnToggleLoading);
        View mask = root.findViewById(R.id.loadingMask);
        ProgressBar progress = root.findViewById(R.id.progressBar);
        if (btn == null || mask == null || progress == null) {
            return;
        }
        final boolean[] visible = {false};
        btn.setOnClickListener(v -> {
            visible[0] = !visible[0];
            int visibility = visible[0] ? View.VISIBLE : View.GONE;
            mask.setVisibility(visibility);
            progress.setVisibility(visibility);
        });
    }

    private static void initFrameAnimation(View root) {
        ImageView imageView = root.findViewById(R.id.ivFrameAnimation);
        Button btn = root.findViewById(R.id.btnStartAnimation);
        if (imageView == null || btn == null) {
            return;
        }
        AnimationDrawable drawable = (AnimationDrawable) imageView.getBackground();
        btn.setOnClickListener(v -> {
            if (drawable != null) {
                if (drawable.isRunning()) {
                    drawable.stop();
                }
                drawable.start();
            }
        });
    }

    private static void initNestedScroll(View root) {
        RecyclerView recyclerView = root.findViewById(R.id.recyclerNested);
        if (recyclerView == null) {
            return;
        }
        List<String> items = createItems(15, "嵌套列表项 %d");
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(new SimpleAdapter(items));
    }

    private static void initRecyclerLinear(View root) {
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        if (recyclerView == null) {
            return;
        }
        List<String> items = createItems(20, "线性列表项 %d");
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(new SimpleAdapter(items));
    }

    private static void initRecyclerGrid(View root) {
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        if (recyclerView == null) {
            return;
        }
        List<String> items = createItems(16, "网格 %d");
        recyclerView.setLayoutManager(new GridLayoutManager(root.getContext(), 2));
        recyclerView.setAdapter(new SimpleAdapter(items));
    }

    private static void initRecyclerStaggered(View root) {
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        if (recyclerView == null) {
            return;
        }
        List<String> items = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("瀑布流 ").append(i);
            if (i % 3 == 0) {
                sb.append("\n多行文本\n模拟高度差异");
            }
            items.add(sb.toString());
        }
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(new SimpleAdapter(items));
    }

    private static void initViewStub(View root) {
        Button btn = root.findViewById(R.id.btnShowError);
        ViewStub stub = root.findViewById(R.id.stubError);
        if (btn == null || stub == null) {
            return;
        }
        final boolean[] inflated = {false};
        btn.setOnClickListener(v -> {
            if (!inflated[0]) {
                View errorView = stub.inflate();
                TextView tvError = errorView.findViewById(R.id.tvError);
                if (tvError != null) {
                    tvError.setText("网络请求失败，请稍后重试");
                }
                inflated[0] = true;
            }
        });
    }

    private static void initComposeRecycler(View root) {
        RecyclerView recyclerView = root.findViewById(R.id.recyclerComposeList);
        if (recyclerView == null) {
            return;
        }
        List<String> items = createItems(20, "Compose LazyColumn 等价项 %d");
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(new SimpleAdapter(items));
    }

    private static void initState(View root) {
        TextView tvCount = root.findViewById(R.id.tvCount);
        Button btn = root.findViewById(R.id.btnIncrement);
        if (tvCount == null || btn == null) {
            return;
        }
        final int[] count = {0};
        btn.setOnClickListener(v -> {
            count[0]++;
            tvCount.setText(String.format(Locale.getDefault(), "计数: %d", count[0]));
        });
    }

    private static void initAnimation(View root) {
        View box = root.findViewById(R.id.animatedBox);
        Button btn = root.findViewById(R.id.btnAnimate);
        if (box == null || btn == null) {
            return;
        }
        final boolean[] expanded = {false};
        btn.setOnClickListener(v -> {
            expanded[0] = !expanded[0];
            int targetSize = expanded[0] ? 200 : 100;
            int startColor = expanded[0] ? 0xFF3F51B5 : 0xFFE53935;
            int endColor = expanded[0] ? 0xFFE53935 : 0xFF3F51B5;

            ValueAnimator sizeAnimator = ValueAnimator.ofInt(box.getWidth(), dpToPx(root, targetSize));
            sizeAnimator.setDuration(300);
            sizeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            sizeAnimator.addUpdateListener(animation -> {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = box.getLayoutParams();
                params.width = value;
                params.height = value;
                box.setLayoutParams(params);
            });

            ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
            colorAnimator.setDuration(300);
            colorAnimator.addUpdateListener(animation ->
                    box.setBackgroundColor((int) animation.getAnimatedValue()));

            sizeAnimator.start();
            colorAnimator.start();
        });
    }

    private static List<String> createItems(int count, String format) {
        List<String> items = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            items.add(String.format(Locale.getDefault(), format, i));
        }
        return items;
    }

    private static int dpToPx(View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
