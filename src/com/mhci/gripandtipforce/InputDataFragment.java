package com.mhci.gripandtipforce;

import android.R.integer;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.TextView;


public class InputDataFragment extends Fragment {
	
	private final static int numFields = 7;
	private Resources mRes = null;
	private String packageName = null;
	public View findViewByStr(View viewToSearchIn, String name) {
		if(mRes == null) {
			mRes = getResources();
		}
			
		int resId = mRes.getIdentifier(name, "id", packageName);
		return viewToSearchIn.findViewById(resId);
		
	}
	private SharedPreferences preferences;
	private TextView[] fieldViews = null;
	
	private void updateIDView(View fragmentView, String userID) {
		((TextView)fragmentView.findViewById(R.id.Number_ID)).setText(userID);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		packageName = getActivity().getPackageName();
		preferences = getActivity().getSharedPreferences(ProjectConfig.Key_Preference_UserInfo, Context.MODE_PRIVATE);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View fragmentView = inflater.inflate(R.layout.fragment_input_data, container, false);
		
		Resources res = getResources();
		String fieldPrefix = "Disp_";
		
		//find the largest width
		int maxWidth = 0;
		fieldViews = new TextView[numFields];
		for(int i = 1;i <= numFields;i++) {
			TextView txtView = (TextView)findViewByStr(fragmentView, fieldPrefix + i);
			fieldViews[i-1] = txtView;
			txtView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			 //dont call getWidth or getHeight before view has been drawn.
			int currentWidth = txtView.getMeasuredWidth();
			Log.d("testWidth", currentWidth + "");
			if(maxWidth < currentWidth) {
				maxWidth = currentWidth;
			}
		}
	
		for(int i = 0;i < numFields;i++) {
			fieldViews[i].setWidth(maxWidth);
		}
		
		String userID = preferences.getString(ProjectConfig.Key_Preference_UserID, null);
		int userIDInNum = -1;
		if(userID == null) { //first time
			userIDInNum = 1;
		}
		else {
			userIDInNum = Integer.valueOf(userID) + 1;
		}
		updateIDView(fragmentView, userIDInNum + "");
		return fragmentView;
	}
}
