package com.example.currentlearnlayuot;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * System Bar 演示页：专门用于在 Layout Inspector 中观察
 * statusBarBackground 与 navigationBarBackground。
 *
 * 注意：本页不使用 EdgeToEdge，以便系统按照主题颜色绘制状态栏和导航栏，
      让学生能直观看到 statusBarColor / navigationBarColor 的效果。
 */
public class SystemBarDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_bar_demo);

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.demo_status_bar));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.demo_nav_bar));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("System Bar 演示");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
