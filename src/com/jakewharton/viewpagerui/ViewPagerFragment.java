package com.jakewharton.viewpagerui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mhci.gripandtipforce.InputDataActivity;
import com.mhci.gripandtipforce.R;
import com.mhci.gripandtipforce.ExperimentActivity;
public final class ViewPagerFragment extends Fragment {
    private static final String KEY_CONTENT = "ViewPagerFragment:Content";
    private static final String[] contentStrings = new String[] {
    		"歡迎使用本系統",
    		"此評量需約30分鐘，請確認你處於舒適的坐姿且不被干擾",
    		"評量時，需使用我們提供的筆將指定的文字抄寫至空格中",
    		"準備好了，請按開始"
    };
    
    public static ViewPagerFragment newInstance(int pageIndex) {
    		ViewPagerFragment fragment = new ViewPagerFragment();
    		fragment.mContent = contentStrings[pageIndex];
    		fragment.mPageIndex = pageIndex;
    		return fragment;
    }

    private String mContent = "???";
    private int mPageIndex = 0;
    private Context mContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
        
        mContext = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {    		
    		View fragmentView = inflater.inflate(R.layout.fragment_viewpager, container, false);
    		TextView textView = (TextView) fragmentView.findViewById(R.id.explanation_text);
    		textView.setText(mContent);
    		Button startButton = (Button)fragmentView.findViewById(R.id.startButton);
    		if(mPageIndex == contentStrings.length - 1) {
    			startButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						//Intent intent = new Intent(mContext,ExperimentActivity.class);
						Intent intent = new Intent(mContext,InputDataActivity.class);
						startActivity(intent);
					}
			});
    			startButton.setVisibility(View.VISIBLE);
    		}
    		else {
    			startButton.setVisibility(View.GONE);
    		}
    		
        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
}
