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
        setContentView(R.layout.activity_swipableview);

    }
}