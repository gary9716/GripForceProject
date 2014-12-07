package com.mhci.gripandtipforce;

import java.util.HashMap;

import com.samsung.samm.common.SObjectStroke;
import com.samsung.spen.lib.input.SPenEventLibrary;
import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SPenTouchListener;

import android.R.integer;
import android.app.Activity;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends Activity {

	public final static String DEBUG_TAG = MainActivity.class.toString();
	private final static int numCharBoxesInRow = 5; //dont forget to modify these two numbers
	private final static int numOfWritableCharBoxRows = 1;
	
	private final int TOOL_UNKNOWN = 0;
	private final int TOOL_FINGER = 1;
	private final int TOOL_PEN = 2;
	private final int TOOL_PEN_ERASER = 3;
	private final String [] toolNames = new String[]{"unknown","finger","pen","eraser"};
	private final static int WritableCharBoxedWidth = 117; //dp on Galaxy 10.1 tab
	private final static int WritableCharBoxedHeight = 117;
	
	private SettingStrokeInfo mStrokeInfoPen;
	private TextView mPressure = null;
	private boolean isToCleanMode = false;
	private SCanvasView[][] mCharBoxes = new SCanvasView[numOfWritableCharBoxRows][numCharBoxesInRow];
	private int[][] mCharsGroupID = new int[][]{
			{R.id.char1_group,R.id.char2_group,R.id.char3_group,R.id.char4_group,R.id.char5_group}
	};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        
    }
    
    private void initUI() {
    	setContentView(R.layout.activity_main);
        for(int i = 0;i < numOfWritableCharBoxRows;i++) {
        	for(int j = 0;j < numCharBoxesInRow;j++) {
            	mCharBoxes[i][j] = new SCanvasView(this);        
            	mCharBoxes[i][j].createSCanvasView(WritableCharBoxedWidth, WritableCharBoxedHeight);
            	//just disable the hover effect in the system setting
            	mCharBoxes[i][j].setSCanvasHoverPointerStyle(SCanvasConstants.SCANVAS_HOVERPOINTER_STYLE_NONE);
            	mCharBoxes[i][j].setSPenTouchListener(sPenTouchListener);
            	mCharBoxes[i][j].setOnClickListener(scanvasOnClickListener);
            	mCharBoxes[i][j].setBackgroundResource(R.drawable.textview_border);
            	((LinearLayout)findViewById(mCharsGroupID[i][j])).addView(mCharBoxes[i][j], WritableCharBoxedWidth, WritableCharBoxedHeight);
        	}
        }
        mPressure = (TextView)findViewById(R.id.pressureIndicator);
        int[] btnIDs = new int[]{R.id.penBtn,R.id.eraserBtn,R.id.cleanBtn};
        
        View.OnClickListener btnOnClickListener = new View.OnClickListener() {
			private void setCanvasModeWithAllSCanvas(int canvasMode) {
				//due to Homogeneity, we could check first one to know the others
				if(mCharBoxes[0][0].getCanvasMode() == canvasMode) { 
					return;
				}
				
				//
				for(int i = 0;i < numOfWritableCharBoxRows;i++) { 
					for(int j = 0;j < numCharBoxesInRow;j++) {
						mCharBoxes[i][j].setCanvasMode(canvasMode);
					}
				}
				return;
			}
        	
        	@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
        		Button btn;
        		
        		try {
        			btn = (Button)view;
        		}
        		catch(Exception e) {
        			//Log.d(MainActivity.this.DEBUG_TAG, e.getMessage());
        			Log.d(MainActivity.this.DEBUG_TAG,"should not see any other view except button using this listener");
        			return;
        		}
        		
        		btn.setClickable(false); //
        		int id = view.getId();
				if(id == R.id.penBtn) {
					setCanvasModeWithAllSCanvas(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
				}
				else if(id == R.id.eraserBtn){
					setCanvasModeWithAllSCanvas(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
				}
				else if(id == R.id.cleanBtn) {
					isToCleanMode = !isToCleanMode;
					if(isToCleanMode) {
						btn.setText("cancel");
					}
					else {
						btn.setText("clean");
					}
				}
				
				btn.setClickable(true);
			}
		};
		
        for(int id : btnIDs) {
        	Button button = (Button)findViewById(id);
        	button.setOnClickListener(btnOnClickListener);
        }
        
        
        //seems that we need Pen SDK not only S Pen SDK
        mStrokeInfoPen = new SettingStrokeInfo();
		mStrokeInfoPen.setStrokeStyle(SObjectStroke.SAMM_STROKE_STYLE_PENCIL);
		mStrokeInfoPen.setStrokeWidth(15);
		
		mCharBoxes[0][0].setSettingViewStrokeInfo(mStrokeInfoPen);
        
    }
    
    private SPenTouchListener sPenTouchListener = new SPenTouchListener() {
		
    	//returning false means it will dispatch event to SCanvasView for drawing
		@Override
		public boolean onTouchPenEraser(View view, MotionEvent event) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onTouchPen(View view, MotionEvent event) {
			// TODO Auto-generated method stub
			// TODO compute sampling rate here
			mPressure.setText(event.getPressure() + "");
			return false;
		}
		
		@Override
		public boolean onTouchFinger(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			return true; // disable finger drawing and do palm rejection
		}
		
		@Override
		public void onTouchButtonUp(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onTouchButtonDown(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private View.OnClickListener scanvasOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(isToCleanMode) {
				int canvasID = v.getId();
				try {
					//ask user whether he want to delete
					SCanvasView sCanvasView = (SCanvasView)v;
				}
				catch(Exception e) {
					Log.d(MainActivity.this.DEBUG_TAG, e.getMessage());
				}
				
			
				
			}
			
		}
	};
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
