package com.tac.sawit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DetailBulananPagerAdapter extends FragmentStateAdapter {

    private final int tahun;
    private final int bulan;

    public DetailBulananPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                     int tahun, int bulan) {
        super(fragmentActivity);
        this.tahun = tahun;
        this.bulan = bulan;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return PemasukanFragment.newInstance(tahun, bulan);
        } else {
            return PengeluaranFragment.newInstance(tahun, bulan);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
