package com.jakewharton.viewpagerui;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class ViewPagerFragmentAdapter extends FragmentPagerAdapter{
	private final int mCount = 4; // number of pages
	private final static String appTitle = "兒童書寫評量系統";
	
    public ViewPagerFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return ViewPagerFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return appTitle;
    }

//    public void setCount(int count) {
//        if (count > 0 && count <= 10) {
//            mCount = count;
//            notifyDataSetChanged();
//        }
//    }
}