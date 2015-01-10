package com.mhci.gripandtipforce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

public class ImgFileManager {
	public final static String DEBUG_TAG = ImgFileManager.class.toString();
	private final static String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GripForce/images";
	private static ImgFileManager instance = null;
	
	private Context mContext;
	private File imgDir = null;
	private HandlerThread mThread = null;
	private Handler mThreadHandler = null;
	
	public static ImgFileManager getInstance(Context context) {
		if(instance == null) {
			synchronized (ImgFileManager.class) {
				if(instance == null) {
					instance = new ImgFileManager();
				}
			}
		}
		
		if(context != null) {
			instance.mContext = context;
		}
		
		return instance;
	}
	
	private void initThreadAndHandler() {
		mThread = new HandlerThread("SaveImgThread");
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper());
	}
	
	private enum FuncIndex {
		createOrOpenImgDir,
		saveImgInDir
	};
	
	private String fileNameForImgSavingThread = null;
	private Bitmap bmpForImgSavingThread = null;
	
	public void saveBMP(Bitmap bmp, String filename) {
		bmpForImgSavingThread = bmp;
		fileNameForImgSavingThread = filename;
		funcIndex = FuncIndex.saveImgInDir;
		if(mThreadHandler == null) {
			initThreadAndHandler();
		}
		mThreadHandler.post(taskForThread);
	}
	
	private FuncIndex funcIndex = FuncIndex.createOrOpenImgDir;
	
	public Runnable taskForThread = new Runnable() {
		private void createOrOpenImgDir() {
			imgDir = new File(dirPath);
			if (!imgDir.exists()) {
				if (!imgDir.mkdirs()) {
					Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
					return;
				}
			}
		}
		
		private void saveBMPIntoFile(Bitmap bmp, String fileName) {
			FileOutputStream out = null;
			try {
				String filePath = null;
				if(imgDir != null) {
					filePath = imgDir.getPath() + "/" + fileName;
				}
				else {
					Toast.makeText(mContext, "Image Directory doesn't exist, failed to save image", Toast.LENGTH_LONG).show();
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
			        bmp.recycle();
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			    
			}
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			switch(funcIndex) {
				case createOrOpenImgDir:
					createOrOpenImgDir();
					break;
				case saveImgInDir:
					saveBMPIntoFile(bmpForImgSavingThread, fileNameForImgSavingThread);
					break;
				default:
					Log.d(DEBUG_TAG,"Unknown function index in task for thread");
					break;
			}
		}
	};
	
	public ImgFileManager() {
		initThreadAndHandler();
		funcIndex = FuncIndex.createOrOpenImgDir;
		mThreadHandler.post(taskForThread);
	}
	
}
