package com.example.currentlearnlayuot.viewload;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.currentlearnlayuot.R;

/**
 * 视图加载流程演示入口页。
 * 提供三个按钮分别进入 Activity / Fragment / Adapter 触发方式的演示页面。
 */
public class ViewLoadHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_load_hub);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("视图加载流程演示");
            }
        }

        findViewById(R.id.btnActivity).setOnClickListener(v ->
                startActivity(new Intent(this, ActivityLoadActivity.class)));
        findViewById(R.id.btnFragment).setOnClickListener(v ->
                startActivity(new Intent(this, FragmentLoadActivity.class)));
        findViewById(R.id.btnAdapter).setOnClickListener(v ->
                startActivity(new Intent(this, AdapterLoadActivity.class)));
        findViewById(R.id.btnPlainProof).setOnClickListener(v ->
                startActivity(new Intent(this, PlainActivityProofActivity.class)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
