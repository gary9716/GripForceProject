package com.mhci.gripandtipforce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

public class ImgFileManager extends FileManager{
	public final static String DEBUG_TAG = ImgFileManager.class.toString();
	private Context mContext;
	private File imgDir = null;
	
	public ImgFileManager(FileDirInfo dirInfo, Context context) {
		super(context,FileType.Image);
		//remain flexible for developer to do IO operation in another thread
		readUserConfig();
		
//		if(!FileDirInfo.isExternalStorageWritable()) {
//			Toast.makeText(context, "資料無法寫入指定資料夾,請再次確認設定無誤", Toast.LENGTH_LONG).show();
//		}
		
		mContext = context;
		initThreadAndHandler();
		imgDir = new File(dirInfo.getDirPath());
		if (!imgDir.exists()) {
			if (!imgDir.mkdirs()) {
				Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}
	
	private HandlerThread mThread = null;
	private Handler mThreadHandler = null;
	
	private void initThreadAndHandler() {
		mThread = new HandlerThread("SaveImgThread");
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper());
	}
	
	public void saveBMP(Bitmap bmp, String fileName) {
		SaveBMPTask task = new SaveBMPTask(bmp, fileName);
		mThreadHandler.post(task);
	}
	
	private class SaveBMPTask implements Runnable {
		
		private Bitmap mBitmapToSave;
		private String fileNameForSaving;
		
		public SaveBMPTask(Bitmap bmp,String fileName) {
			// TODO Auto-generated constructor stub
			mBitmapToSave = bmp;
			fileNameForSaving = fileName;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			FileOutputStream out = null;
			try {
				File file = null;
				if(imgDir != null) {
					file = new File(imgDir.getPath(), fileNameForSaving);
				}
				else {
					Toast.makeText(mContext, "Image Directory doesn't exist, failed to save image", Toast.LENGTH_LONG).show();
					return;
				}
				
				out = new FileOutputStream(file);
				mBitmapToSave.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
			    // PNG is a lossless format, the compression factor (100) is ignored
			} catch (Exception e) {
			    e.printStackTrace();
			} finally {
			    try {
			        if (out != null) {
			            out.close();
			        }
			        mBitmapToSave.recycle();
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			    
			}
		}
		
	}
	
	public static String getImgFileName(String userID,int grade,int charIndex) {
		return userID + "_" + grade + "_" + (charIndex + 1) + ProjectConfig.imgFileExtension;
	}
	
}
