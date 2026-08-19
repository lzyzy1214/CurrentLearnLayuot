package com.example.currentlearnlayuot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * XML 加载实时验证页：在一个页面内同时展示 Activity / Fragment / Adapter
 * 三种入口的加载结果，并把日志直接显示在页面上。
 */
public class XmlInflationDemoActivity extends AppCompatActivity {

    private TextView tvActivityLog;
    private TextView tvFragmentLog;
    private TextView tvAdapterLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml_inflation_demo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("XML 加载实时验证");
            }
        }

        tvActivityLog = findViewById(R.id.tvActivityLog);
        tvFragmentLog = findViewById(R.id.tvFragmentLog);
        tvAdapterLog = findViewById(R.id.tvAdapterLog);

        runActivityProof();
        runFragmentProof();
        runAdapterProof();
    }

    /**
     * 验证 Activity.setContentView 把 View 挂到 DecorView 的 content 容器
     */
    private void runActivityProof() {
        StringBuilder sb = new StringBuilder();
        sb.append("【Activity 入口】setContentView 后的挂载位置\n");

        View decorView = getWindow().getDecorView();
        ViewGroup content = decorView.findViewById(android.R.id.content);

        sb.append("content 容器：").append(content.getClass().getName()).append("\n");
        sb.append("content 子 View 数：").append(content.getChildCount()).append("\n");
        for (int i = 0; i < content.getChildCount(); i++) {
            View child = content.getChildAt(i);
            sb.append("  [").append(i).append("] ")
                    .append(child.getClass().getName())
                    .append("\n");
        }

        tvActivityLog.setText(sb.toString());
    }

    /**
     * 验证 Fragment 的 View 最终挂到 Activity 的 content 容器
     */
    private void runFragmentProof() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentContainer, new ProofDemoFragment());
        ft.commitNow();

        // Fragment 会在内部回调中把父链写回 tvFragmentLog
    }

    /**
     * 验证 Adapter 的 item View 挂到 RecyclerView
     */
    private void runAdapterProof() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> data = new ArrayList<>();
        data.add("Item 1");
        data.add("Item 2");

        ProofDemoAdapter adapter = new ProofDemoAdapter(data, (log) -> {
            tvAdapterLog.append(log + "\n");
        });
        recyclerView.setAdapter(adapter);
    }

    /**
     * 内部 Fragment，用于验证 Fragment 入口
     */
    public static class ProofDemoFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_proof_demo, container, false);

            view.post(() -> {
                StringBuilder sb = new StringBuilder();
                sb.append("【Fragment 入口】onCreateView 返回 View 的父链\n");

                View current = view;
                while (current != null) {
                    sb.append(current.getClass().getSimpleName());
                    ViewParent parent = current.getParent();
                    if (parent instanceof View) {
                        sb.append(" -> ");
                        current = (View) parent;
                    } else {
                        break;
                    }
                }

                TextView tv = requireActivity().findViewById(R.id.tvFragmentLog);
                if (tv != null) {
                    tv.setText(sb.toString());
                }
            });

            return view;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
