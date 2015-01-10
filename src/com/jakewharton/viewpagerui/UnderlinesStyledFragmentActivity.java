package com.jakewharton.viewpagerui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.UnderlinePageIndicator;
import com.mhci.gripandtipforce.R;

public class UnderlinesStyledFragmentActivity extends FragmentActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_underlines);

        ViewPagerFragmentAdapter mAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());

        ViewPager mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        UnderlinePageIndicator indicator = (UnderlinePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(mPager);
        indicator.setSelectedColor(0x00B715);
        indicator.setBackgroundColor(0xFFCCCCCC);
        indicator.setFades(false);
        //indicator.setFadeDelay(1000);
        //indicator.setFadeLength(1000);
    }
}