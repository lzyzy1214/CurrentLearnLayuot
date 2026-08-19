package com.example.currentlearnlayuot.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.currentlearnlayuot.data.Chapter;
import com.example.currentlearnlayuot.data.ExampleItem;
import com.example.currentlearnlayuot.fragment.ExampleFragment;

import java.util.List;

/**
 * 章节详情页 ViewPager2 适配器
 */
public class ExamplePagerAdapter extends FragmentStateAdapter {

    private final List<ExampleItem> examples;

    public ExamplePagerAdapter(@NonNull FragmentActivity fragmentActivity,
                               @NonNull Chapter chapter) {
        super(fragmentActivity);
        this.examples = chapter.examples;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ExampleFragment.newInstance(examples.get(position));
    }

    @Override
    public int getItemCount() {
        return examples == null ? 0 : examples.size();
    }

    public CharSequence getPageTitle(int position) {
        if (examples == null || position < 0 || position >= examples.size()) {
            return "";
        }
        return examples.get(position).title;
    }
}
