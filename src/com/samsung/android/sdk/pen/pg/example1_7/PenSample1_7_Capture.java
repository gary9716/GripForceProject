package com.samsung.android.sdk.pen.pg.example1_7;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.mhci.gripandtipforce.BGImgManager;
import com.mhci.gripandtipforce.R;

public class PenSample1_7_Capture extends Activity {
	
	private String DEBUG_TAG = PenSample1_7_Capture.class.getName().toString();
	
    private final int REQUEST_CODE_SELECT_IMAGE_BACKGROUND = 100;

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
    
    private TextView mPressure = null;
    
    private int mToolType = SpenSurfaceView.TOOL_SPEN;
    private MediaScannerConnection msConn = null;
    
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
    private RelativeLayout[][] spenViewsContainer = new RelativeLayout[numOfWritableCharBoxRows][numCharBoxesInRow];
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
	
    private SpenTouchListener sPenTouchListener = new SpenTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			mPressure.setText(event.getPressure() + "");
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
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        mContext = this;

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
        
        mPressure = (TextView)findViewById(R.id.pressureIndicator);
        //RelativeLayout spenViewContainer = (RelativeLayout) findViewById(R.id.spenViewContainer);
        for(int i = 0;i < numOfWritableCharBoxRows;i++) {
        	for(int j = 0;j < numCharBoxesInRow;j++) {
        		spenViewsContainer[i][j] = (RelativeLayout) findViewById(mWritableCharsContainerID[i][j]);
        	}
        }
        
        for(int i = 0;i < numOfWritableCharBoxRows;i++) {
        	for(int j = 0;j < numCharBoxesInRow;j++) {
        		
        		mCharBoxes[i][j] = new SpenSurfaceView(this);   
            	mCharBoxes[i][j].setClickable(true);
            	//to disable hover effect, just disable the hover effect in the system setting	
            	//mCharBoxes[i][j].setColorPickerListener(mColorPickerListener);
            	mCharBoxes[i][j].setTouchListener(sPenTouchListener);
            	mCharBoxes[i][j].setLongPressListener(new customizedLongPressedListener(mCharBoxes[i][j]));
            	mCharBoxes[i][j].setZoomable(false);
            	
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
        
        BGImgManager manager = new BGImgManager(this);
        String imgFileName = manager.getBGImgFileName();
        
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
        		//mSpenPageDocs[i][j].setHistoryListener(mHistoryListener);
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
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
            Toast.makeText(mContext, "Device does not support Spen. \n You can draw stroke by finger",
                    Toast.LENGTH_SHORT).show();
        }
    }
    

    private final OnClickListener mCaptureBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            closeSettingView();
            captureSpenSurfaceView();
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

    private void closeSettingView() {
        // Close all the setting views.
        mEraserSettingView.setVisibility(SpenSurfaceView.GONE);
        mPenSettingView.setVisibility(SpenSurfaceView.GONE);
    }

    private void setBtnEnabled(boolean clickable) {
        // Enable or disable all the buttons.
        mPenBtn.setEnabled(clickable);
        mEraserBtn.setEnabled(clickable);
    }

    private void callGalleryForInputImage(int nRequestCode) {
        // Get an image from Gallery.
        try {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, nRequestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "Cannot find gallery.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(mContext, "Cannot find the image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Process background image request.
            if (requestCode == REQUEST_CODE_SELECT_IMAGE_BACKGROUND) {
                // Get the image's URI and set the file path to the background image.
                Uri imageFileUri = data.getData();
                Cursor cursor = getContentResolver().query(Uri.parse(imageFileUri.toString()), null, null, null, null);
                cursor.moveToNext();
                String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));

                mSpenPageDoc.setBackgroundImage(imagePath);
                mSpenSurfaceView.update();
            }
        }
    }

    private void captureSpenSurfaceView() {
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
    }

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