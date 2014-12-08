package com.mhci.gripandtipforce;

import java.io.IOException;
import java.util.HashMap;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingEraserInfo;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.settingui.SpenSettingEraserLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;

import android.R.integer;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	public final static String DEBUG_TAG = MainActivity.class.toString();
	private final static int numCharBoxesInRow = 5; //dont forget to modify these two numbers
	private final static int numOfWritableCharBoxRows = 1;
	
	private final String [] toolNames = new String[]{"unknown","finger","pen","eraser"};
	private final static int WritableCharBoxedWidth = 117; //dp on Galaxy 10.1 tab
	private final static int WritableCharBoxedHeight = 117;
	
	
	private int mToolType = SpenSurfaceView.TOOL_SPEN;
	private SpenSettingEraserLayout mEraserSettingView;
	//private SpenSurfaceView mSpenSurfaceView;
    //private SpenSettingPenLayout mPenSettingView;
    
    private Context mContext = null;
	private TextView mPressure = null;
	private boolean isToCleanMode = false;
	private SpenPageDoc mSpenPageDoc;
	private SpenSurfaceView[][] mCharBoxes = new SpenSurfaceView[numOfWritableCharBoxRows][numCharBoxesInRow];
	private int[][] mCharsGroupID = new int[][]{
			{R.id.char1_group,R.id.char2_group,R.id.char3_group,R.id.char4_group,R.id.char5_group}
	};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initUI();
    }
    
    private void initUI() {
    	setContentView(R.layout.activity_main);
        //init SPen
    	boolean isSpenFeatureEnabled = false;
    	Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            if(!spenPackage.isFeatureEnabled(Spen.DEVICE_PEN)) {
            	Log.d(MainActivity.this.DEBUG_TAG, "s pen is not available,terminating app");
            	finish();
            }
        }  
        catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Spen.", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }
    	
        /*
        FrameLayout spenViewContainer = (FrameLayout) findViewById(R.id.spenViewContainer);
        RelativeLayout spenViewLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);

        // Create PenSettingView
        if (android.os.Build.VERSION.SDK_INT > 19) {
            mPenSettingView = new SpenSettingPenLayout(mContext, new String(), spenViewLayout);
        } else {
            mPenSettingView = new SpenSettingPenLayout(getApplicationContext(), new String(), spenViewLayout);
        }
        if (mPenSettingView == null) {
            Toast.makeText(getApplicationContext(), "Cannot create new PenSettingView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        */
        
        RelativeLayout settingView = (RelativeLayout)findViewById(R.id.settingView);
        // Create EraserSettingView
        if (android.os.Build.VERSION.SDK_INT > 19) {
            mEraserSettingView = new SpenSettingEraserLayout(mContext, new String(), settingView);
        } else {
            mEraserSettingView = new SpenSettingEraserLayout(getApplicationContext(), new String(), settingView);
        }

        if (mEraserSettingView == null) {
            Toast.makeText(mContext, "Cannot create new EraserSettingView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        
        //Initialize Pen Settings
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        penInfo.color = Color.BLACK;
        penInfo.size = 15;
        
        // Initialize Eraser Settings
        SpenSettingEraserInfo eraserInfo = new SpenSettingEraserInfo();
        eraserInfo.size = 10;
        mEraserSettingView.setInfo(eraserInfo);
        mEraserSettingView.setViewMode(SpenSettingEraserLayout.VIEW_MODE_NORMAL);
        mEraserSettingView.setVisibility(View.VISIBLE);
        
        // Get the dimension of the device screen.
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);
		// Create SpenNoteDoc
        SpenNoteDoc mSpenNoteDoc = null;
        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, WritableCharBoxedWidth, WritableCharBoxedHeight);
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc",
                Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
		// Add a Page to NoteDoc, get an instance, and set it to the member variable.
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        //mSpenPageDoc.setBackgroundColor(0xFFD6E6F5);
        mSpenPageDoc.clearHistory();
        //Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.textview_border);
		//mSpenPageDoc.setVolatileBackgroundImage(image);
		
    	for(int i = 0;i < numOfWritableCharBoxRows;i++) {
        	for(int j = 0;j < numCharBoxesInRow;j++) {
            	mCharBoxes[i][j] = new SpenSurfaceView(this);       
            	mEraserSettingView.setCanvasView(mCharBoxes[i][j]);
            	
            	//to disable hover effect, just disable the hover effect in the system setting	
            	mCharBoxes[i][j].setTouchListener(sPenTouchListener);
            	mCharBoxes[i][j].setOnClickListener(charBoxOnClickListener);
            	//currently we disable finger's function. Maybe we could use it as eraser in the future.
            	mCharBoxes[i][j].setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_NONE);
            	mCharBoxes[i][j].setToolTypeAction(SpenSurfaceView.TOOL_SPEN, SpenSurfaceView.ACTION_STROKE);
            	mCharBoxes[i][j].setPenSettingInfo(penInfo);
            	mCharBoxes[i][j].setEraserSettingInfo(eraserInfo);
            	mCharBoxes[i][j].setPageDoc(mSpenPageDoc, true);
            	//mCharBoxes[i][j].setBackgroundResource(R.drawable.textview_border);
            	((LinearLayout)findViewById(mCharsGroupID[i][j])).addView(mCharBoxes[i][j], WritableCharBoxedWidth, WritableCharBoxedHeight);
        	}
        }
        mPressure = (TextView)findViewById(R.id.pressureIndicator);
        View.OnClickListener btnOnClickListener = new View.OnClickListener() {
			private void setSPenToolActionWithAllCanvases(int toolAction) {
				//due to Homogeneity, we could check first one to know the others
				
				if(mCharBoxes[0][0].getToolTypeAction(SpenSurfaceView.TOOL_SPEN) == toolAction) { 
					return;
				}
				
				for(int i = 0;i < numOfWritableCharBoxRows;i++) { 
					for(int j = 0;j < numCharBoxesInRow;j++) {
						mCharBoxes[i][j].setToolTypeAction(SpenSurfaceView.TOOL_SPEN, toolAction);
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
					setSPenToolActionWithAllCanvases(SpenSurfaceView.ACTION_STROKE);
				}
				else if(id == R.id.eraserBtn){
					setSPenToolActionWithAllCanvases(SpenSurfaceView.ACTION_ERASER);
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
		
		int[] btnIDs = new int[]{R.id.penBtn,R.id.eraserBtn,R.id.cleanBtn};
        for(int id : btnIDs) {
        	Button button = (Button)findViewById(id);
        	button.setOnClickListener(btnOnClickListener);
        }
        
    }
    
    private SpenTouchListener sPenTouchListener = new SpenTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			mPressure.setText(event.getPressure() + "");
			return false;
		}
	};
	
	private View.OnClickListener charBoxOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(isToCleanMode) {
				int canvasID = v.getId();
				try {
					//ask user whether he want to delete
					SpenSurfaceView penCanvasView = (SpenSurfaceView)v;
					
				}
				catch(Exception e) {
					Log.d(MainActivity.this.DEBUG_TAG, e.getMessage());
				}
			}
			
		}
	};
	
	protected void onDestroy() {
		super.onDestroy();
		/*
        if (mPenSettingView != null) {
            mPenSettingView.close();
        }
        if (mEraserSettingView != null) {
            mEraserSettingView.close();
        }
        */
		for(int i = 0;i < numOfWritableCharBoxRows;i++) {
			for(int j = 0;j < numCharBoxesInRow;j++) {
				if(mCharBoxes[i][j] != null) {
					mCharBoxes[i][j].close();
					mCharBoxes[i][j] = null;
				}
			}
		}
	}
	
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
