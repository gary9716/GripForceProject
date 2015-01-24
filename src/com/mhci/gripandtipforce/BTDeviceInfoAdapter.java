package com.mhci.gripandtipforce;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BTDeviceInfoAdapter extends ArrayAdapter<String> {

	private LayoutInflater inflater = null;
    //private ArrayList<String> mInfoContainer;
	private int mSelectedIndex;
    private int selectedColor;
    private int defaultColor;
	
	public BTDeviceInfoAdapter(Context context, int resource) {
		super(context, resource);
		// TODO Auto-generated constructor stub
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//mInfoContainer = new ArrayList<String>();
		mSelectedIndex = -1;
		selectedColor = context.getResources().getColor(R.color.pressed_color);
		defaultColor = context.getResources().getColor(R.color.default_color);
	}

	public void setSelectedIndex(int selectedIndex) {
		if(mSelectedIndex == selectedIndex) {
			return;
		}
		
		mSelectedIndex = selectedIndex;
		notifyDataSetChanged();
	}
	
	public int getSelectedIndex() {
		return mSelectedIndex;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
			TextView txtV = (TextView)convertView.findViewById(android.R.id.text1);
			txtV.setText(getItem(position));
		}
		
		if(mSelectedIndex != position) {
			convertView.setBackgroundColor(defaultColor);
		}
		else if(mSelectedIndex == position) {
			convertView.setBackgroundColor(selectedColor);
		}
		
		return convertView;
	}

}
