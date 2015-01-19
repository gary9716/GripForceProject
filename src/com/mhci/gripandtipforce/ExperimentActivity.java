package com.mhci.gripandtipforce;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
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
import com.mhci.gripandtipforce.R;

public class ExperimentActivity extends Activity {

	private String DEBUG_TAG = ExperimentActivity.class.getName().toString();

	private Context mContext = null;
	private SpenSettingPenLayout mPenSettingView;
	private SpenSettingEraserLayout mEraserSettingView;

	private ImageView mPenBtn;
	private ImageView mEraserBtn;
	private ImageView mCleanBtn;
	private ImageView mSaveBtn;

	private TextView mPenTipInfo = null;

	private int mToolType = SpenSurfaceView.TOOL_SPEN;

	private final static int numCharBoxesInRow = 5; //dont forget to modify these two numbers
	private final static int numWritableCharBoxRows = 1;
	private boolean isToCleanMode = false;

	private int[][] mWritableCharsContainerID = new int[][]{
				{R.id.WritableCharContainer1, 
				R.id.WritableCharContainer2, 
				R.id.WritableCharContainer3, 
				R.id.WritableCharContainer4, 
				R.id.WritableCharContainer5}
	};
	
	private SpenSurfaceView[][] mCharBoxes = new SpenSurfaceView[numWritableCharBoxRows][numCharBoxesInRow];

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

	private class customizedLongPressedListener implements SpenLongPressListener {
		private SpenSurfaceView currentlySelectedSurfaceView = null;

		private SpenSurfaceView bindedSurfaceView = null;

		public customizedLongPressedListener(SpenSurfaceView surfaceView) {
			bindedSurfaceView = surfaceView;
		}
		
		private void cleanCurrentlySelectedView() {
			if(currentlySelectedSurfaceView != null) {
				SpenPageDoc model = viewModelMap.get(currentlySelectedSurfaceView);
				model.removeAllObject();
				currentlySelectedSurfaceView.update();
			}
			return;
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
				
				builder.setNegativeButton("cancel", null);

				// 3. Get the AlertDialog from create()
				(builder.create()).show();

			}

		}

	}

	private int totalChars = 0;

	private SpenTouchListener sPenTouchListener = new SpenTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			mPenTipInfo.setText("p:" + event.getPressure() + ",x:" + event.getX() + ",y:" + event.getY() + ",ts:" + (System.currentTimeMillis() - fixedDateInMillis));
			return false;
		}
	};

	private View.OnClickListener mBtnOnClickListener = new View.OnClickListener() {
		private void setSPenToolActionWithAllCanvases(int toolAction) {
			//due to Homogeneity, we could check first one to know the others

			if(mCharBoxes[0][0].getToolTypeAction(SpenSurfaceView.TOOL_SPEN) == toolAction) { 
				return;
			}

			for(int i = 0;i < numWritableCharBoxRows;i++) { 
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

	private SpenNoteDoc mSpenNoteDoc;
	private SpenPageDoc[][] mSpenPageDocs = new SpenPageDoc[numWritableCharBoxRows][numCharBoxesInRow];
	private HashMap<SpenSurfaceView, SpenPageDoc> viewModelMap = new HashMap<SpenSurfaceView, SpenPageDoc>(numCharBoxesInRow * numWritableCharBoxRows);
	
	//private HashMap<Integer,Pair<Integer, Integer>> viewIndexMap = new HashMap<Integer,Pair<Integer, Integer>>(numCharBoxesInRow * numOfWritableCharBoxRows);
	
	private ImgFileManager imgFileManager = null;
	private TxtFileManager txtFileManager = null;
	private int charIndex;
	
	private long fixedDateInMillis = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Calendar calendar = Calendar.getInstance();
		calendar.set(2015, Calendar.JANUARY, 1);
		fixedDateInMillis = calendar.getTimeInMillis();
		
		setContentView(R.layout.activity_experiment);
		mContext = this;
		
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
		FileDirInfo dirInfo = new FileDirInfo(FileType.Image, null, null);
		imgFileManager = new ImgFileManager(dirInfo, this);
		dirInfo.setFileType(FileType.Log);
		dirInfo.setOtherInfo((numCharBoxesInRow * numWritableCharBoxRows) + "");
		txtFileManager = new TxtFileManager(dirInfo, this);
		
		mPenTipInfo = (TextView)findViewById(R.id.penTipInfo);
		
		for(int i = 0;i < numWritableCharBoxRows;i++) {
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
		
		for(int i = 0;i < numWritableCharBoxRows;i++) {
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
				viewModelMap.put(mCharBoxes[i][j], mSpenPageDocs[i][j]);

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
			Toast.makeText(mContext, "Device does not support Spen. \n You can draw stroke by finger",
					Toast.LENGTH_SHORT).show();
		}
		
	}
	
	private void previousPage() {
		charIndex = charIndex - numCharBoxesInRow*numWritableCharBoxRows;
		if(charIndex < 0) {
			charIndex = 0;
		}
	}
	
	private void nextPage() {
		charIndex = charIndex + numCharBoxesInRow*numWritableCharBoxRows;
		if(charIndex >= totalChars) {
			charIndex = totalChars - numCharBoxesInRow*numWritableCharBoxRows;
		}
	}
	
	private void loadChineseCharsDependOnGrade(int grade) {
		
		
	}

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
		imgFileManager.saveBMP(imgBitmap, fileName);
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
}