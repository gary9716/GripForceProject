package com.mhci.gripandtipforce;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BTDeviceInfoAdapter extends ArrayAdapter<String> {

	private LayoutInflater inflater = null;
	private int mSelectedIndex;
    private int selectedColor;
    private int defaultColor;
	//private ArrayList<String> dataContainer;
	
	public BTDeviceInfoAdapter(Context context, int resource) {
		super(context, resource);
		// TODO Auto-generated constructor stub
		//inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater = LayoutInflater.from(context);
		//mInfoContainer = new ArrayList<String>();
		mSelectedIndex = -1;
		selectedColor = context.getResources().getColor(R.color.pressed_color);
		defaultColor = context.getResources().getColor(R.color.default_color);
		//dataContainer = new ArrayList<String>();
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
	public void clear() {
		// TODO Auto-generated method stub
		super.clear();
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
		String content = getItem(position);
		if(content != null) {
			TextView txtV = (TextView)convertView.findViewById(android.R.id.text1);
			txtV.setText(content);
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
