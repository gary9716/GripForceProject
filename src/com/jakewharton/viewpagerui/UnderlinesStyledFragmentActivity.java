package com.jakewharton.viewpagerui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.UnderlinePageIndicator;
import com.mhci.gripandtipforce.CustomizedBaseFragmentActivity;
import com.mhci.gripandtipforce.ProjectConfig;
import com.mhci.gripandtipforce.R;

public class UnderlinesStyledFragmentActivity extends CustomizedBaseFragmentActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //code for close the app and clean up all activities
        if(getIntent().getBooleanExtra("EXIT", false)) {
        		showSystemBar();
        		finish();
        }
        
        ProjectConfig.initSomeVars(this); // some variables need to be init inside context
        
        setContentView(R.layout.activity_swipableview);
        
    }
}