package com.mhci.gripandtipforce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class ImgFileManager {
	public final static String DEBUG_TAG = ImgFileManager.class.toString();
	private Context mContext;
	
	private File imgDir = null;
	
	public ImgFileManager(Context context) {
		mContext = context;
		
		imgDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SPen/images");
		if (!imgDir.exists()) {
			if (!imgDir.mkdirs()) {
				Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		
	}
	
	public void saveBMPIntoFile(Bitmap bmp, String fileName) {
		FileOutputStream out = null;
		try {
			String filePath = null;
			if(imgDir != null) {
				filePath = imgDir.getPath() + "/" + fileName;
			}
			else {
				Log.d(DEBUG_TAG,"imgDir is null, failed to save bmp file");
				return;
			}
			out = new FileOutputStream(filePath);
			bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
		    // PNG is a lossless format, the compression factor (100) is ignored
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
