package com.samsung.android.sdk.pen.pg.example1_7;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingEraserInfo;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenLongPressListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.settingui.SpenSettingEraserLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.mhci.gripandtipforce.ImgFileManager;
import com.mhci.gripandtipforce.R;

public class PenSample1_7_Capture extends Activity {

	private String DEBUG_TAG = PenSample1_7_Capture.class.getName().toString();

	private Context mContext = null;
	private SpenNoteDoc mSpenNoteDoc;
	private SpenPageDoc mSpenPageDoc;
	private SpenSurfaceView mSpenSurfaceView;
	private SpenSettingPenLayout mPenSettingView;
	private SpenSettingEraserLayout mEraserSettingView;

	private ImageView mPenBtn;
	private ImageView mEraserBtn;
	private ImageView mCleanBtn;
	private ImageView mSaveBtn;

	private TextView mPenTipInfo = null;

	private int mToolType = SpenSurfaceView.TOOL_SPEN;

	private final static int numCharBoxesInRow = 5; //dont forget to modify these two numbers
	private final static int numOfWritableCharBoxRows = 1;
	private boolean isToCleanMode = false;

	private int[][] mWritableCharsContainerID = new int[][]{
				{R.id.WritableCharContainer1, 
				R.id.WritableCharContainer2, 
				R.id.WritableCharContainer3, 
				R.id.WritableCharContainer4, 
				R.id.WritableCharContainer5}
	};
	
	private SpenSurfaceView[][] mCharBoxes = new SpenSurfaceView[numOfWritableCharBoxRows][numCharBoxesInRow];
	private SpenSurfaceView currentlySelectedSurfaceView = null;

	private SpenSettingPenInfo penInfo;
	private SpenSettingEraserInfo eraserInfo;
	private void initSettingInfo2() {

		// Initialize Pen settings
		penInfo = new SpenSettingPenInfo();
		penInfo.color = Color.BLACK;
		penInfo.size = 1;

		// Initialize Eraser settings
		eraserInfo = new SpenSettingEraserInfo();
		eraserInfo.size = 30;

	}

	private void cleanCurrentlySelectedView() {
		if(currentlySelectedSurfaceView != null) {
			SpenPageDoc model = viewModelMap.get(currentlySelectedSurfaceView);
			model.removeAllObject();
			currentlySelectedSurfaceView.update();
		}
		return;
	}

	private class customizedLongPressedListener implements SpenLongPressListener {
		private SpenSurfaceView bindedSurfaceView = null;

		public customizedLongPressedListener(SpenSurfaceView surfaceView) {
			bindedSurfaceView = surfaceView;
		}

		@Override
		public void onLongPressed(MotionEvent arg0) {
			// TODO Auto-generated method stub
			currentlySelectedSurfaceView = bindedSurfaceView;

			if(isToCleanMode) {

				currentlySelectedSurfaceView.setSelected(true);

				// 1. Instantiate an AlertDialog.Builder with its constructor
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

				// 2. Chain together various setter methods to set the dialog characteristics
				builder.setMessage("You're going to clean the handwriting you just clicked.\nAre you sure?");

				// Add the buttons
				builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK button
						cleanCurrentlySelectedView();
					}
				});

				// 3. Get the AlertDialog from create()
				(builder.create()).show();

			}

		}

	}

	private Date mDate = new Date();
	private int totalChars = 0;

	private SpenTouchListener sPenTouchListener = new SpenTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			mPenTipInfo.setText("p:" + event.getPressure() + ",x:" + event.getX() + ",y:" + event.getY() + ",ts:" + mDate.getTime());
			return false;
		}
	};

	private View.OnClickListener mBtnOnClickListener = new View.OnClickListener() {
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

			int id = view.getId();
			isToCleanMode = false;
			if(id == R.id.penBtn) {
				setSPenToolActionWithAllCanvases(SpenSurfaceView.ACTION_STROKE);
				selectButton(mPenBtn);
			}
			else if(id == R.id.eraserBtn){
				setSPenToolActionWithAllCanvases(SpenSurfaceView.ACTION_ERASER);
				selectButton(mEraserBtn);
			}
			else if(id == R.id.cleanBtn) {
				isToCleanMode = true;
				setSPenToolActionWithAllCanvases(SpenSurfaceView.ACTION_STROKE);
				selectButton(mCleanBtn);
			}

		}
	};

	private SpenPageDoc[][] mSpenPageDocs = new SpenPageDoc[numOfWritableCharBoxRows][numCharBoxesInRow];
	private HashMap<SpenSurfaceView, SpenPageDoc> viewModelMap = new HashMap<SpenSurfaceView, SpenPageDoc>(numCharBoxesInRow * numOfWritableCharBoxRows);
	
	//private HashMap<Integer,Pair<Integer, Integer>> viewIndexMap = new HashMap<Integer,Pair<Integer, Integer>>(numCharBoxesInRow * numOfWritableCharBoxRows);
	
	private ImgFileManager imgFileManager = null;
	
	private int charIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture2);
		mContext = this;
		imgFileManager = new ImgFileManager(mContext);
		
		charIndex = 0;
		
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
		initSettingInfo2();

		mPenTipInfo = (TextView)findViewById(R.id.penTipInfo);
		//RelativeLayout spenViewContainer = (RelativeLayout) findViewById(R.id.spenViewContainer);
		
		for(int i = 0;i < numOfWritableCharBoxRows;i++) {
			for(int j = 0;j < numCharBoxesInRow;j++) {

				mCharBoxes[i][j] = new SpenSurfaceView(this);   
				mCharBoxes[i][j].setClickable(true);
				//to disable hover effect, just disable the hover effect in the system setting	
				mCharBoxes[i][j].setTouchListener(sPenTouchListener);
				mCharBoxes[i][j].setLongPressListener(new customizedLongPressedListener(mCharBoxes[i][j]));
				mCharBoxes[i][j].setZoomable(false);

				//currently we disable finger's function. Maybe we could use it as eraser in the future.
				mCharBoxes[i][j].setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_NONE);
				mCharBoxes[i][j].setToolTypeAction(SpenSurfaceView.TOOL_SPEN, SpenSurfaceView.ACTION_STROKE);
				mCharBoxes[i][j].setPenSettingInfo(penInfo);
				mCharBoxes[i][j].setEraserSettingInfo(eraserInfo);
				((RelativeLayout)findViewById(mWritableCharsContainerID[i][j])).addView(mCharBoxes[i][j]);
				//System.runFinalization();
				//Pair indexPair = new Pair<Integer,Integer>(i, j);
				//viewIndexMap.put(mCharBoxes[i][j].getId(),indexPair);
			}
		}
		
		
		// Get the dimension of the device screen.
		Display display = getWindowManager().getDefaultDisplay();
		Rect rect = new Rect();
		display.getRectSize(rect);
		// Create SpenNoteDoc
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

		//String imgFileName = manager.getBGImgFileName();
		String imgFileName = null;
		//SpenPageDoc docToSet = mSpenNoteDoc.appendPage();
		
		for(int i = 0;i < numOfWritableCharBoxRows;i++) {
			for(int j = 0;j < numCharBoxesInRow;j++) {
				// Add a Page to NoteDoc, get an instance, and set it to the member variable.
				
				if(imgFileName != null) {
					mSpenPageDocs[i][j] = mSpenNoteDoc.insertPage(i * numCharBoxesInRow + j, 0, imgFileName, SpenPageDoc.BACKGROUND_IMAGE_MODE_FIT);
				}
				else {
					mSpenPageDocs[i][j] = mSpenNoteDoc.insertPage(i * numCharBoxesInRow + j);
					mSpenPageDocs[i][j].setBackgroundColor(0xFFD6E6F5);
				}
				mSpenPageDocs[i][j].clearHistory();
				SpenPageDoc docToSet = mSpenPageDocs[i][j];
				
				mCharBoxes[i][j].setPageDoc(docToSet, true);
				//viewModelMap.put(mCharBoxes[i][j], mSpenPageDocs[i][j]);

			}
		}

		// Set a button
		mPenBtn = (ImageView) findViewById(R.id.penBtn);
		mPenBtn.setOnClickListener(mBtnOnClickListener);

		mEraserBtn = (ImageView) findViewById(R.id.eraserBtn);
		mEraserBtn.setOnClickListener(mBtnOnClickListener);

		mCleanBtn = (ImageView) findViewById(R.id.cleanBtn);
		mCleanBtn.setOnClickListener(mBtnOnClickListener);

		selectButton(mPenBtn);

		if (isSpenFeatureEnabled == false) {
			mToolType = SpenSurfaceView.TOOL_FINGER;
			mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
			Toast.makeText(mContext, "Device does not support Spen. \n You can draw stroke by finger",
					Toast.LENGTH_SHORT).show();
		}
		
	}
	
	private void previousPage() {
		charIndex = charIndex - numCharBoxesInRow*numOfWritableCharBoxRows;
		if(charIndex < 0) {
			charIndex = 0;
		}
	}
	
	private void nextPage() {
		charIndex = charIndex + numCharBoxesInRow*numOfWritableCharBoxRows;
		if(charIndex >= totalChars) {
			charIndex = totalChars - numCharBoxesInRow*numOfWritableCharBoxRows;
		}
	}
	
	private void loadChineseCharsDependOnGrade(int grade) {
		
		
	}
	
	private final OnClickListener mCaptureBtnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
		}
	};

	private void selectButton(View v) {
		// Enable or disable the button according to the current mode.
		mPenBtn.setSelected(false);
		mEraserBtn.setSelected(false);
		mCleanBtn.setSelected(false);

		v.setSelected(true);

		//closeSettingView();
	}

	private void setBtnEnabled(boolean clickable) {
		// Enable or disable all the buttons.
		mPenBtn.setEnabled(clickable);
		mEraserBtn.setEnabled(clickable);
	}

	private void captureSpenSurfaceView(int row, int col, String fileName) {	
		Bitmap imgBitmap = mCharBoxes[row][col].captureCurrentView(true);
		imgFileManager.saveBMPIntoFile(imgBitmap, fileName);
		imgBitmap.recycle();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mSpenNoteDoc != null && mSpenPageDoc.isRecording()) {
			mSpenPageDoc.stopRecord();
		}

		if (mPenSettingView != null) {
			mPenSettingView.close();
		}
		if (mEraserSettingView != null) {
			mEraserSettingView.close();
		}
		if (mSpenSurfaceView != null) {
			if (mSpenSurfaceView.getReplayState() == SpenSurfaceView.REPLAY_STATE_PLAYING) {
				mSpenSurfaceView.stopReplay();
			}
			mSpenSurfaceView.close();
			mSpenSurfaceView = null;
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
}