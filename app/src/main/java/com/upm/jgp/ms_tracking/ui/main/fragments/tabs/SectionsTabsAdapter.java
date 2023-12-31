package com.upm.jgp.healthywear.ui.main.fragments.tabs;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.upm.jgp.healthywear.R;


/**
 * A SectionsTabsAdapter that returns a fragment corresponding to
 * one of the tabs.
 *
 * Using 4 Tabs necessary to update second device interface
 *
 * @author Jorge Garcia Paredes (yoryidan)
 * @version 222
 * @since 2020
 */
public class SectionsTabsAdapter extends FragmentPagerAdapter {
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3,R.string.tab_text_4,R.string.tab_text_5, R.string.tab_text_6, R.string.ellipsis};
    private final Context mContext;

    public SectionsTabsAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a TabFragment (defined as a static inner class below).
        return TabFragment.newInstance(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 5 total pages.
        return 7;
    }
}