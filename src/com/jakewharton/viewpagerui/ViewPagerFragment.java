package com.jakewharton.viewpagerui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mhci.gripandtipforce.R;
public final class ViewPagerFragment extends Fragment {
    private static final String KEY_CONTENT = "ViewPagerFragment:Content";
    private static final String[] contentStrings = new String[] {
    		"歡迎使用本系統",
    		"此評量需約30分鐘，請確認你處於舒適的坐姿且不被干擾",
    		"評量時，需使用我們提供的筆將指定的文字抄寫至空格中",//maybe we could provide an example below
    		"準備好了，請按開始"
    };
    
    public static ViewPagerFragment newInstance(int pageIndex) {
    		ViewPagerFragment fragment = new ViewPagerFragment();
    		fragment.mContent = contentStrings[pageIndex];
    		
    		return fragment;
    }

    private String mContent = "???";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        TextView text = new TextView(getActivity());
//        text.setGravity(Gravity.CENTER);
//        text.setText(mContent);
//        text.setTextSize(20 * getResources().getDisplayMetrics().density);
//        text.setPadding(20, 20, 20, 20);
//
//        LinearLayout layout = new LinearLayout(getActivity());
//        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//        layout.setGravity(Gravity.CENTER);
//        layout.addView(text);
    		
    		View fragmentView = inflater.inflate(R.layout.fragment_viewpager, container, false);
    		TextView textView = (TextView) fragmentView.findViewById(R.id.explanation_text);
    		textView.setText(mContent);
    	
        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
}
