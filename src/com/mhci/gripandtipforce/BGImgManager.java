package com.mhci.gripandtipforce;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BGImgManager {
	public final static String DEBUG_TAG = BGImgManager.class.toString();
	
	private final static String BGFileNameForWritableChars = "writableCharBounds";
	private String mAbsoluteBGImgFilePath = null;
	private Context mContext;
	
	public BGImgManager(Context context) {
		mContext = context;
		
		if(!checkBGImgFileIsExisted()) {
			saveResourceIntoFile();
		}
	}
	
	public String getBGImgFileName() {
		return mAbsoluteBGImgFilePath;
	}
	
	public boolean checkBGImgFileIsExisted() {
		
		try {
			FileInputStream inputStream = mContext.openFileInput(BGFileNameForWritableChars);
			return true;
		}
		catch(FileNotFoundException e) {
			Log.d(DEBUG_TAG,e.getLocalizedMessage());
			return false;
		}
		
	}
	
	private void saveResourceIntoFile() {
		
		FileOutputStream out = null;
		try {
			Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.textview_border);
		    out = mContext.openFileOutput(BGFileNameForWritableChars, Context.MODE_PRIVATE);
		    bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
		    // PNG is a lossless format, the compression factor (100) is ignored
		    if(checkBGImgFileIsExisted()) {
		    	mAbsoluteBGImgFilePath = mContext.getFilesDir() + "/" + BGFileNameForWritableChars;
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (out != null) {
		            out.close();
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		
	}
	
}
