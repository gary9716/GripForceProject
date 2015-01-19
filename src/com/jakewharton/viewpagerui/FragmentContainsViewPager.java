package com.jakewharton.viewpagerui;

import com.mhci.gripandtipforce.R;
import com.viewpagerindicator.UnderlinePageIndicator;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentContainsViewPager extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		FragmentActivity parent = getActivity();
		View currentView = inflater.inflate(R.layout.fragment_viewpager_underlines, container, false);
		
		ViewPagerFragmentAdapter mAdapter = new ViewPagerFragmentAdapter(parent.getSupportFragmentManager());
        
		ViewPager mPager = (ViewPager)currentView.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        UnderlinePageIndicator indicator = (UnderlinePageIndicator)currentView.findViewById(R.id.indicator);
        indicator.setViewPager(mPager);
        indicator.setSelectedColor(0x00B715);
        indicator.setBackgroundColor(0xFFCCCCCC);
        indicator.setFades(false);
        //indicator.setFadeDelay(1000);
        //indicator.setFadeLength(1000);
		
		return currentView;
	
	}
}
