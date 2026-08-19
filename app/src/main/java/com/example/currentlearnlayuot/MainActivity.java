package com.example.currentlearnlayuot;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currentlearnlayuot.adapter.ChapterAdapter;
import com.example.currentlearnlayuot.data.Chapter;
import com.example.currentlearnlayuot.data.ChapterRepository;
import com.example.currentlearnlayuot.xmlflow.XmlLoadingFlowHubActivity;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/**
 * 主界面：展示章节列表并支持搜索
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CHAPTER_ID = "chapter_id";

    private ChapterAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        List<Chapter> chapters = ChapterRepository.getChapters();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChapterAdapter(chapters, chapter -> {
            Intent intent = new Intent(MainActivity.this, ChapterActivity.class);
            intent.putExtra(EXTRA_CHAPTER_ID, chapter.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("搜索章节");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filter(newText);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_system_bar_demo) {
            startActivity(new Intent(this, SystemBarDemoActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_view_tree_info) {
            startActivity(new Intent(this, ViewTreeInfoActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_xml_inflation) {
            startActivity(new Intent(this, XmlInflationActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_xml_inflation_demo) {
            startActivity(new Intent(this, XmlInflationDemoActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_view_load_flow) {
            startActivity(new Intent(this, com.example.currentlearnlayuot.viewload.ViewLoadHubActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_xml_loading_flow_docs) {
            startActivity(new Intent(this, XmlLoadingFlowHubActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
