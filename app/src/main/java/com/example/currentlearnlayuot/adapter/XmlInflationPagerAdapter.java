package com.example.currentlearnlayuot.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.currentlearnlayuot.fragment.XmlInflationFlowFragment;
import com.example.currentlearnlayuot.fragment.XmlInflationProofFragment;
import com.example.currentlearnlayuot.fragment.XmlInflationStepsFragment;

/**
 * XML 加载流程 ViewPager2 适配器
 */
public class XmlInflationPagerAdapter extends FragmentStateAdapter {

    public XmlInflationPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new XmlInflationFlowFragment();
            case 1:
                return new XmlInflationStepsFragment();
            case 2:
                return new XmlInflationProofFragment();
            default:
                return new XmlInflationFlowFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
