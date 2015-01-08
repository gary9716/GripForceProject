package com.mhci.gripandtipforce;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingEraserInfo;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc.HistoryListener;
import com.samsung.android.sdk.pen.document.SpenPageDoc.HistoryUpdateInfo;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.pen.SpenPenManager;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.settingui.SpenSettingEraserLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingEraserLayout.EventListener;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
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
    private SpenNoteDoc mSpenNoteDoc;
    
    private SpenSettingPenLayout mPenSettingView;
    private SpenSettingEraserLayout mEraserSettingView;

    private ImageView mPenBtn;
    private ImageView mEraserBtn;
    private ImageView mUndoBtn;
    private ImageView mRedoBtn;
    private ImageView mCaptureBtn;
    private Context mContext = null;
	private TextView mPressure = null;
	
	private SpenSettingPenInfo penInfo;
	private SpenSettingEraserInfo eraserInfo;
	
	private SpenSurfaceView[][] mCharBoxes = new SpenSurfaceView[numOfWritableCharBoxRows][numCharBoxesInRow];
	private SpenPageDoc[][] mSpenPageDoc = new SpenPageDoc[numOfWritableCharBoxRows][numCharBoxesInRow];
	private HashMap<SpenSurfaceView, SpenPageDoc> viewModelMap = new HashMap<SpenSurfaceView, SpenPageDoc>(numCharBoxesInRow * numOfWritableCharBoxRows);
	
	private SpenSurfaceView currentlySelectedSurfaceView = null;
	
	private int[][] mWritableCharsContainerID = new int[][]{
			{R.id.WritableCharContainer1, 
		     R.id.WritableCharContainer2, 
		     R.id.WritableCharContainer3, 
		     R.id.WritableCharContainer4, 
		     R.id.WritableCharContainer5}
	};
	
	private RelativeLayout[][] spenViewsContainer = new RelativeLayout[numOfWritableCharBoxRows][numCharBoxesInRow];
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initUI();
    }
    
    private void initUI() {
    	setContentView(R.layout.activity_main2);
        
    	// Initialize Spen
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (SsdkUnsupportedException e) {
            if (SDKUtils.processUnsupportedException(this, e) == true) {
                return;
            }
        } catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Spen.", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }
    	
        RelativeLayout viewsContainer = (RelativeLayout) findViewById(R.id.views_container);
        for(int i = 0;i < numOfWritableCharBoxRows;i++) {
        	for(int j = 0;j < numCharBoxesInRow;j++) {
        		spenViewsContainer[i][j] = (RelativeLayout) findViewById(mWritableCharsContainerID[i][j]);
        	}
        }
        
        // Create PenSettingView
        //I think relative layout that contain spenView should be use for SpenSettingLayout Initialization
        //temporary I set first one as its control target, later would be adjusted in OnClickListener of spenView.
        /*
        if (android.os.Build.VERSION.SDK_INT > 19) {
            mPenSettingView = new SpenSettingPenLayout(mContext, new String(), spenViewsContainer[0][0]);
        } else {
            mPenSettingView = new SpenSettingPenLayout(getApplicationContext(), new String(), spenViewsContainer[0][0]);
        }
        if (mPenSettingView == null) {
            Toast.makeText(mContext, "Cannot create new PenSettingView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        
        // Create EraserSettingView
        if (android.os.Build.VERSION.SDK_INT > 19) {
            mEraserSettingView = new SpenSettingEraserLayout(mContext, new String(), spenViewsContainer[0][0]);
        } else {
            mEraserSettingView = new SpenSettingEraserLayout(getApplicationContext(), new String(), spenViewsContainer[0][0]);
        }
        if (mEraserSettingView == null) {
            Toast.makeText(mContext, "Cannot create new EraserSettingView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        
        viewsContainer.addView(mPenSettingView);
        viewsContainer.addView(mEraserSettingView);
		
        */
        
        initSettingInfo();
        
        //mPenSettingView.setInfo(penInfo);
        //mEraserSettingView.setInfo(eraserInfo);
        
        for(int i = 0;i < numOfWritableCharBoxRows;i++) {
        	for(int j = 0;j < numCharBoxesInRow;j++) {
        		
        		// Add a Page to NoteDoc, get an instance, and set it to the member variable.
        		
        		//mSpenPageDoc[i][j] = mSpenNoteDoc.appendPage();
        		//mSpenPageDoc[i][j].setBackgroundColor(0xFFD6E6F5);
                //mSpenPageDoc[i][j].clearHistory();
        		//mSpenPageDoc[i][j].setHistoryListener(mHistoryListener);
        		
        		mCharBoxes[i][j] = new SpenSurfaceView(this);   
            	
        		viewModelMap.put(mCharBoxes[i][j], mSpenPageDoc[i][j]);
        		
            	//mPenSettingView.setCanvasView(mCharBoxes[i][j]);
            	//mEraserSettingView.setCanvasView(mCharBoxes[i][j]);
            	
            	//to disable hover effect, just disable the hover effect in the system setting	
            	mCharBoxes[i][j].setColorPickerListener(mColorPickerListener);
            	mCharBoxes[i][j].setTouchListener(sPenTouchListener);
            	mCharBoxes[i][j].setOnClickListener(surfaceViewOnClickListener);
            	
            	//currently we disable finger's function. Maybe we could use it as eraser in the future.
            	mCharBoxes[i][j].setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_NONE);
            	mCharBoxes[i][j].setToolTypeAction(SpenSurfaceView.TOOL_SPEN, SpenSurfaceView.ACTION_STROKE);
            	mCharBoxes[i][j].setPenSettingInfo(penInfo);
            	mCharBoxes[i][j].setEraserSettingInfo(eraserInfo);
            	spenViewsContainer[i][j].addView(mCharBoxes[i][j]);
        	}
        }
        
        // Get the dimension of the device screen.
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);
		// Create SpenNoteDoc
        SpenNoteDoc mSpenNoteDoc = null;
        try {
        	mSpenNoteDoc = new SpenNoteDoc(mContext, rect.width(), rect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        
//        SpenPageDoc docForTest = mSpenNoteDoc.appendPage();
//        docForTest.clearHistory();
//    	SpenSurfaceView viewForTest = new SpenSurfaceView(this);
//    	viewsContainer.addView(viewForTest);
//    	viewForTest.setPageDoc(docForTest, true);
//    	
    	
        mSpenPageDoc[0][0] = mSpenNoteDoc.appendPage();
		mSpenPageDoc[0][0].setBackgroundColor(0xFFD6E6F5);
        mSpenPageDoc[0][0].clearHistory();
		mSpenPageDoc[0][0].setHistoryListener(mHistoryListener);
        
    	for(int i = 0;i < numOfWritableCharBoxRows;i++) {
    		for(int j = 0;j< numCharBoxesInRow;j++) {
    			SpenPageDoc docToSet = mSpenPageDoc[0][0];
    			mCharBoxes[i][j].setPageDoc(docToSet, true);
    		}
    	}
    	
    	mPressure = (TextView)findViewById(R.id.penTipInfo);
        
        /*
        mEraserSettingView.setEraserListener(mEraserListener);
        
        // Set a button
        
        mPenBtn = (ImageView) findViewById(R.id.penBtn);
        mPenBtn.setOnClickListener(mPenBtnClickListener);

        mEraserBtn = (ImageView) findViewById(R.id.eraserBtn);
        mEraserBtn.setOnClickListener(mEraserBtnClickListener);

        mUndoBtn = (ImageView) findViewById(R.id.undoBtn);
        mUndoBtn.setOnClickListener(undoNredoBtnClickListener);
        mUndoBtn.setEnabled(mSpenPageDoc[0][0].isUndoable());

        mRedoBtn = (ImageView) findViewById(R.id.redoBtn);
        mRedoBtn.setOnClickListener(undoNredoBtnClickListener);
        mRedoBtn.setEnabled(mSpenPageDoc[0][0].isRedoable());


        mCaptureBtn = (ImageView) findViewById(R.id.captureBtn);
        mCaptureBtn.setOnClickListener(mCaptureBtnClickListener);
		
        selectButton(mPenBtn);
        */
        
        /*
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
        */
    	if(isSpenFeatureEnabled == false) {
            Toast.makeText(mContext,
                "Device does not support Spen.",
                Toast.LENGTH_SHORT).show();
        }
        
    }
    
    private OnClickListener surfaceViewOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.d(MainActivity.DEBUG_TAG,"clicked");
			
			try {
				currentlySelectedSurfaceView = (SpenSurfaceView)v;
			}
			catch(Exception e) {
				Log.d(MainActivity.DEBUG_TAG,e.getLocalizedMessage());
			}
		}
	};
    
    private SpenTouchListener sPenTouchListener = new SpenTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			mPressure.setText(event.getPressure() + "");
			return false;
		}
	};
	
	private void initSettingInfo() {
		
        // Initialize Pen settings
        penInfo = new SpenSettingPenInfo();
        penInfo.color = Color.BLACK;
        penInfo.size = 15;
        penInfo.name = SpenPenManager.SPEN_INK_PEN;
        
        // Initialize Eraser settings
        eraserInfo = new SpenSettingEraserInfo();
        eraserInfo.size = 30;
       
    }
	
	private void setAllSurfaceViewWith(int toolType, int action_type) {
		if(mCharBoxes[0][0].getToolTypeAction(toolType) == action_type) {
			return;
		}
		
		for(int i = 0;i < numOfWritableCharBoxRows;i++) {
			for(int j = 0;j < numCharBoxesInRow;j++) {
				mCharBoxes[i][j].setToolTypeAction(toolType, action_type);
			}
		}
	}
	
	private final OnClickListener mPenBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	SpenSurfaceView surfaceView = mCharBoxes[0][0];
            
        	// When Spen is in stroke (pen) mode
            if (surfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_STROKE) {
                // If PenSettingView is open, close it.
                if (mPenSettingView.isShown()) {
                    mPenSettingView.setVisibility(View.GONE);
                    // If PenSettingView is not open, open it.
                } else {
                    mPenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_EXTENSION);
                    mPenSettingView.setVisibility(View.VISIBLE);
                }
                // If Spen is not in stroke (pen) mode, change it to stroke mode.
            } else {
                selectButton(mPenBtn);
                setAllSurfaceViewWith(mToolType, SpenSurfaceView.ACTION_STROKE);
            }
        }
    };

    private final OnClickListener mEraserBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	SpenSurfaceView surfaceView = mCharBoxes[0][0];
            
        	// When Spen is in eraser mode
            if (surfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_ERASER) {
                // If EraserSettingView is open, close it.
                if (mEraserSettingView.isShown()) {
                    mEraserSettingView.setVisibility(View.GONE);
                    // If EraserSettingView is not open, open it.
                } else {
                    mEraserSettingView.setViewMode(SpenSettingEraserLayout.VIEW_MODE_NORMAL);
                    mEraserSettingView.setVisibility(View.VISIBLE);
                }
                // If Spen is not in eraser mode, change it to eraser mode.
            } else {
                selectButton(mEraserBtn);
                setAllSurfaceViewWith(mToolType, SpenSurfaceView.ACTION_ERASER);
            }
        }
    };
	
	private final OnClickListener mCaptureBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            closeSettingView();
            captureSpenSurfaceView();
        }
    };

    private final OnClickListener undoNredoBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	if(currentlySelectedSurfaceView == null)
        		return;
        	
            SpenPageDoc latestlyWrittenDoc = viewModelMap.get(currentlySelectedSurfaceView);
        	
            // Undo button is clicked
            if (v.equals(mUndoBtn)) {
                if (latestlyWrittenDoc.isUndoable()) {
                    HistoryUpdateInfo[] userData = latestlyWrittenDoc.undo();
                    currentlySelectedSurfaceView.updateUndo(userData);
                }
                // Redo button is clicked
            } else if (v.equals(mRedoBtn)) {
                if (latestlyWrittenDoc.isRedoable()) {
                    HistoryUpdateInfo[] userData = latestlyWrittenDoc.redo();
                    currentlySelectedSurfaceView.updateRedo(userData);
                }
            }
        }
    };

    private final SpenColorPickerListener mColorPickerListener = new SpenColorPickerListener() {
        @Override
        public void onChanged(int color, int x, int y) {
            // Set the color from the Color Picker to the setting view.
            if (mPenSettingView != null) {
                SpenSettingPenInfo penInfo = mPenSettingView.getInfo();
                penInfo.color = color;
                mPenSettingView.setInfo(penInfo);
            }
        }
    };

    private final EventListener mEraserListener = new EventListener() {
        @Override
        public void onClearAll() {
            // ClearAll button action routines of EraserSettingView
            if(currentlySelectedSurfaceView != null) {
            	SpenPageDoc modelDoc = viewModelMap.get(currentlySelectedSurfaceView);
            	modelDoc.removeAllObject();
            	currentlySelectedSurfaceView.update();
            }
        }
    };

    private final HistoryListener mHistoryListener = new HistoryListener() {
        @Override
        public void onCommit(SpenPageDoc page) {
        }

        @Override
        public void onUndoable(SpenPageDoc page, boolean undoable) {
            // Enable or disable the button according to the availability of undo.
            mUndoBtn.setEnabled(undoable);
        }

        @Override
        public void onRedoable(SpenPageDoc page, boolean redoable) {
            // Enable or disable the button according to the availability of redo.
            mRedoBtn.setEnabled(redoable);
        }
    };
	
	private void selectButton(View v) {
        // Enable or disable the button according to the current mode.
        mPenBtn.setSelected(false);
        mEraserBtn.setSelected(false);

        v.setSelected(true);

        closeSettingView();
    }

    private void closeSettingView() {
        // Close all the setting views.
        mEraserSettingView.setVisibility(SpenSurfaceView.GONE);
        mPenSettingView.setVisibility(SpenSurfaceView.GONE);
    }

    private void setBtnEnabled(boolean clickable) {
        // Enable or disable all the buttons.
        mPenBtn.setEnabled(clickable);
        mEraserBtn.setEnabled(clickable);
        mUndoBtn.setEnabled(clickable);
        mRedoBtn.setEnabled(clickable);
        mCaptureBtn.setEnabled(clickable);
    }
	
	private void captureSpenSurfaceView() {
        /*
		// Set save directory for a captured image.
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SPen/images";
        File fileCacheItem = new File(filePath);
        if (!fileCacheItem.exists()) {
            if (!fileCacheItem.mkdirs()) {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        filePath = fileCacheItem.getPath() + "/CaptureImg.png";

        // Capture an image and save it as bitmap.
        Bitmap imgBitmap = mSpenSurfaceView.captureCurrentView(true);

        OutputStream out = null;
        try {
            // Save a captured bitmap image to the directory.
            out = new FileOutputStream(filePath);
            imgBitmap.compress(CompressFormat.PNG, 100, out);
            Toast.makeText(mContext, "Captured images were stored in the file \'CaptureImg.png\'.", Toast.LENGTH_SHORT)
                    .show();
        } catch (Exception e) {
            Toast.makeText(mContext, "Capture failed.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }

                scanImage(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        imgBitmap.recycle();
        */
    }
	
	private MediaScannerConnection msConn = null;
	private void scanImage(final String imageFileName) {
        msConn = new MediaScannerConnection(mContext, new MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
                msConn = null;
            }
        });
        msConn.connect();
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPenSettingView != null) {
            mPenSettingView.close();
        }
        if (mEraserSettingView != null) {
            mEraserSettingView.close();
        }

        if (mSpenNoteDoc != null) {
            try {
                mSpenNoteDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
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
