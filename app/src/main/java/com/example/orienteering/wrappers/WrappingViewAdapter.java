package com.example.orienteering.wrappers;

import android.icu.text.RelativeDateTimeFormatter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.orienteering.competition.TheRunFragment;
import com.example.orienteering.competition.pickCompetition.PickCompetitionFragment;
import com.example.orienteering.nfts.nftManager.NftManagerFragment;

public class WrappingViewAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 3;
    public static final int POS_0 = 0;
    public static final int POS_1 = 1;
    public static final int POS_2 = 2;

    public WrappingViewAdapter(@NonNull Fragment fragment) {        //TODO skontrolovat ci ok
        super(fragment);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position){

            case POS_0:
                return new NftManagerFragment();

            case POS_1:
                return new PickCompetitionFragment();

            case POS_2:
                return new TheRunFragment();    //todo stats fragment with your runs
            default: return new Fragment();
        }

    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }


    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}
