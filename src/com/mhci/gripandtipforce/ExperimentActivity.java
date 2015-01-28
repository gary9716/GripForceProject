package com.mhci.gripandtipforce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
	
	public final static String Action_update_chars = ExperimentActivity.class.getName() + ".update_chars";
	public final static String Key_ExChars = "ExChars";
	
	private final static String DEBUG_TAG = ExperimentActivity.class.getName();
	
	private Context mContext = null;
	private SpenSettingPenLayout mPenSettingView;
	private SpenSettingEraserLayout mEraserSettingView;

	private ImageView mPenBtn;
	private ImageView mEraserBtn;
	private ImageView mCleanBtn;
	//private ImageView mSaveBtn;
	private ImageView mNextPageBtn;

	private TextView mPenTipInfo = null;

	private int mToolType = SpenSurfaceView.TOOL_SPEN;

	private final static int numCharBoxesInRow = 5; //dont forget to modify these two numbers
	private final static int numWritableCharBoxRows = 1;
	private final static int numCharsInAPage = numCharBoxesInRow * numWritableCharBoxRows;
	private boolean isToCleanMode = false;

	private int[][] mWritableCharsContainerID = new int[][]{
				{R.id.WritableCharContainer1, 
				R.id.WritableCharContainer2, 
				R.id.WritableCharContainer3, 
				R.id.WritableCharContainer4, 
				R.id.WritableCharContainer5}
	};
	
	private SpenSurfaceView[][] mCharBoxes = new SpenSurfaceView[numWritableCharBoxRows][numCharBoxesInRow];

	private TextView[][] mExampleCharsTextView = new TextView[numWritableCharBoxRows][numCharBoxesInRow];
	
	private TextView mPreOrPostInfo = null;
	
	private SpenSettingPenInfo penInfo;
	private SpenSettingEraserInfo eraserInfo;

	private LocalBroadcastManager mLBCManager = null;
	private Handler uiThreadHandler = null;

	private SpenNoteDoc mSpenNoteDoc;
	private SpenPageDoc[][] mSpenPageDocs = new SpenPageDoc[numWritableCharBoxRows][numCharBoxesInRow];
	private HashMap<SpenSurfaceView, SpenPageDoc> viewModelMap = new HashMap<SpenSurfaceView, SpenPageDoc>(numCharBoxesInRow * numWritableCharBoxRows);
	
	private Resources mRes;
	private String packageName;
	
	private ImgFileManager imgFileManager = null;
	private TxtFileManager txtFileManager = null;
	private View mExperimentView = null;
	private View mPreOrPostExperimentView = null;
	private Button mPreOrPostNextPageButton = null;
	private long fixedDateInMillis = 0;
	
	private int charIndex = 0;
	private int mGrade = 1; //default we use first grade
	private int testingSetIndex = 0;
	private ArrayList<Integer> testingSetGradesSeq = null;
	
	private int mUserGrade = 1;
	private String mUserDominantHand = ProjectConfig.rightHand;
	private UIState preOrPostInterfaceState = UIState.preExperimentTrial;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Calendar calendar = Calendar.getInstance();
		calendar.set(2015, Calendar.JANUARY, 1);
		fixedDateInMillis = calendar.getTimeInMillis();
		
		mContext = this;
		uiThreadHandler = new Handler(getMainLooper());
		
		packageName = getPackageName();
		mRes = getResources();
		
		mLBCManager = LocalBroadcastManager.getInstance(mContext);
		IntentFilter filter = new IntentFilter(Action_update_chars);
		mLBCManager.registerReceiver(broadcastReceiver, filter);
		
		//try to make UX better, I decide to put some task in background 
		//and use progress dialog to make user feel this app faster than before or at least knowing the state of current app.
		(new LoadActivityContentAsyncTask(mContext, new BeforeViewShownTask(mContext), null)).execute(); 
	}
	
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
			else if(id == R.id.button_experiment_next_step) {
				if(preOrPostInterfaceState == UIState.preExperimentTrial) {
					if(testingSetIndex >= testingSetGradesSeq.size()) {
						//the test should be over now.
						return;
					}
					charIndex = 0;
					mGrade = testingSetGradesSeq.get(testingSetIndex);
					//loadExCharsFromFile(mGrade);
					loadExCharsFromFile(2);
					setContentView(mExperimentView); //switch to experiment view
					preOrPostInterfaceState = UIState.postExperimentTrial;
				}
				else if(preOrPostInterfaceState == UIState.postExperimentTrial) {
					testingSetIndex++; //next testing set
					showPreExperimentView(testingSetGradesSeq.get(testingSetIndex));
					preOrPostInterfaceState = UIState.preExperimentTrial;
				}
			}

		}
	};
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(action.equals(Action_update_chars)) {
				updateExChars(intent.getStringArrayExtra(Key_ExChars));
			}
		}
	};
	
	private SpenTouchListener sPenTouchListener = new SpenTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			mPenTipInfo.setText("p:" + event.getPressure() + ",x:" + event.getX() + ",y:" + event.getY() + ",ts:" + (System.currentTimeMillis() - fixedDateInMillis));
			return false;
		}
	};
	
	private void setNextPageButtonClickable(boolean enable) {
		mNextPageBtn.setClickable(enable);
	}
	
	private String[] cachedChars = null;
	
	private void loadExCharsFromFile(int grade) {
		cachedChars = null;
		txtFileManager.toLoadChineseChars(grade);
	}
	
	public void updateExChars(String[] exChars) {
		String[] charsUsedForUpdate = null;
		if(exChars == null) {
			if(cachedChars != null) {
				charsUsedForUpdate = cachedChars;
			}
			else {
				Log.d(DEBUG_TAG, "no available chars, updating exChars failed");
				loadExCharsFromFile(mGrade);
				return;
			}
		}
		else {
			charsUsedForUpdate = exChars;
		}
		
		setNextPageButtonClickable(false);
		for(int i = 0;i < numWritableCharBoxRows;i++) {
			for(int j = 0;j < numCharBoxesInRow;j++) {
				int charIndexToRetrieve = charIndex + i * numCharBoxesInRow + j;
				if(charIndexToRetrieve < charsUsedForUpdate.length) {
					mExampleCharsTextView[i][j].setText(charsUsedForUpdate[charIndexToRetrieve]);
				}
				else {
					Log.d(DEBUG_TAG, "out of range, updating exChars failed");
					setNextPageButtonClickable(true);
					return;
				}
				
			}
		}
		setNextPageButtonClickable(true);
		return;
	}
	
	private void previousPage() {
		charIndex = charIndex - numCharsInAPage;
		if(charIndex < 0) {
			charIndex = 0;
		}
		updateExChars(null);
	}
	
	private void nextPage() {
		charIndex = charIndex + numCharsInAPage;
		if(cachedChars != null && charIndex >= cachedChars.length) {
			//switch to next testing set
			
		}
		updateExChars(null);
	}
	
	private void nextTestingSet() {
		cachedChars = null;
		
	}
	
	private void showPreExperimentView(int grade) {
		if(testingSetIndex < testingSetGradesSeq.size()) {
			mPreOrPostInfo.setText("即將開始測試" + grade + "年級的生字，\n準備好後請按下一步");
		}
		else {
			mPreOrPostNextPageButton.setVisibility(View.GONE);
			mPreOrPostInfo.setText("評量到此結束，感謝你的參與");
		}
		setContentView(mPreOrPostExperimentView);
	}
		
	private class BeforeViewShownTask implements Runnable {

		private Context mContext;
		private LayoutInflater inflater;
		
		public BeforeViewShownTask(Context context) {
			// TODO Auto-generated constructor stub
			mContext = context;
			inflater = LayoutInflater.from(mContext);
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			mExperimentView = inflater.inflate(R.layout.activity_experiment, null);
			
			/* Spen Dependent Part Start */
			
			// Initialize Spen
			boolean isSpenFeatureEnabled = false;
			Spen spenPackage = new Spen();
			try {
				spenPackage.initialize(mContext);
				isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
			} catch (SsdkUnsupportedException e) {
				if (SDKUtils.processUnsupportedException(ExperimentActivity.this, e) == true) {
					return;
				}
			} catch (Exception e1) {
				Toast.makeText(mContext, "Cannot initialize Spen.", Toast.LENGTH_SHORT).show();
				e1.printStackTrace();
				finish();
			}
			initSettingInfo2();
			
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

			String imgFileName = null;
			
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

				}
			}
			
			
			uiThreadHandler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					//this part need to be run inside main thread otherwise it would crash.
					//we still need around 2 sec to run this part of code
					for(int i = 0;i < numWritableCharBoxRows;i++) {
						for(int j = 0;j < numCharBoxesInRow;j++) {

							mCharBoxes[i][j] = new SpenSurfaceView(mContext);   
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
							((RelativeLayout)mExperimentView.findViewById(mWritableCharsContainerID[i][j])).addView(mCharBoxes[i][j]);
							
							mCharBoxes[i][j].setPageDoc(mSpenPageDocs[i][j], true);
							viewModelMap.put(mCharBoxes[i][j], mSpenPageDocs[i][j]);
							
						}
					}
				}
			});
			
			if (isSpenFeatureEnabled == false) {
				mToolType = SpenSurfaceView.TOOL_FINGER;
				Toast.makeText(mContext, "Device does not support Spen. \n You can draw stroke by finger",
						Toast.LENGTH_SHORT).show();
			}
			
			/* Spen Dependent Part End */
			
			FileDirInfo dirInfo = new FileDirInfo(FileType.Image, null, null);
			imgFileManager = new ImgFileManager(dirInfo, mContext);
			dirInfo.setFileType(FileType.Log, true);
			dirInfo.setOtherInfo(String.valueOf(numCharBoxesInRow * numWritableCharBoxRows));
			txtFileManager = new TxtFileManager(dirInfo, mContext);
			
			mPenTipInfo = (TextView)mExperimentView.findViewById(R.id.penTipInfo);
			
			// Set a button
			mPenBtn = (ImageView) mExperimentView.findViewById(R.id.penBtn);
			mPenBtn.setOnClickListener(mBtnOnClickListener);

			mEraserBtn = (ImageView) mExperimentView.findViewById(R.id.eraserBtn);
			mEraserBtn.setOnClickListener(mBtnOnClickListener);

			mCleanBtn = (ImageView) mExperimentView.findViewById(R.id.cleanBtn);
			mCleanBtn.setOnClickListener(mBtnOnClickListener);
			
			mNextPageBtn = (ImageView) mExperimentView.findViewById(R.id.nextPageBtn);
			mNextPageBtn.setOnClickListener(mBtnOnClickListener);
			
			for(int i = 0;i < numWritableCharBoxRows;i++) {
				for(int j = 0;j < numCharBoxesInRow;j++) {
					mExampleCharsTextView[i][j] = (TextView)findViewByStr(mExperimentView, "exChar" + (i * numCharBoxesInRow + j + 1));
				}
			}
			
			selectButton(mPenBtn);
			
			mPreOrPostExperimentView = inflater.inflate(R.layout.activity_start_and_end, null);
			mPreOrPostInfo = (TextView)mPreOrPostExperimentView.findViewById(R.id.text_pre_or_post_info);
			mPreOrPostNextPageButton = (Button)mPreOrPostExperimentView.findViewById(R.id.button_experiment_next_step);
			mPreOrPostNextPageButton.setOnClickListener(mBtnOnClickListener);
		
			
			SharedPreferences userInfoPreference = mContext.getSharedPreferences(ProjectConfig.Key_Preference_UserInfo, Context.MODE_PRIVATE);
			
			mUserGrade = 1;
			mUserDominantHand = ProjectConfig.rightHand;
			
			if(userInfoPreference != null) {
				mUserGrade = (int)userInfoPreference.getLong(ProjectConfig.Key_Preference_UserGrade, 1);
				mUserDominantHand = userInfoPreference.getString(ProjectConfig.Key_Preference_UserDominantHand, ProjectConfig.rightHand);
			}
			
			testingSetGradesSeq = ProjectConfig.getTestingGradeSequence(mUserGrade);
			
			testingSetIndex = 0;
			charIndex = 0;
			preOrPostInterfaceState = UIState.preExperimentTrial;
			mGrade = testingSetGradesSeq.get(testingSetIndex);
			
			uiThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					showPreExperimentView(mGrade);
				}
			});
			
		}
		
	}
	
	private View findViewByStr(View viewToSearchIn, String name) {
		
		int resId = mRes.getIdentifier(name, "id", packageName);
		return viewToSearchIn.findViewById(resId);
		
	}
	
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
		
		if(mLBCManager != null) {
			mLBCManager.unregisterReceiver(broadcastReceiver);
		}
		
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