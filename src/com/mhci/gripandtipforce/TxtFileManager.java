package com.mhci.gripandtipforce;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;

import android.R.integer;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

public class TxtFileManager {
	private final static String DEBUG_TAG = TxtFileManager.class.getName();
	private String mDirPath = null;
	private File mFileDir = null;
	private FileType mFileType = FileType.Log;
	private Context mContext;
	private HashMap<Integer, PrintWriter> pwMap = null;
	
	public TxtFileManager(FileDirInfo dirInfo,Context context) {
		mContext = context;
		mDirPath = dirInfo.getDirPath();
		mFileType = dirInfo.getFileType();
		initThreadAndHandler();
		
		switch(mFileType) {
			case Log:
				pwMap = new HashMap<Integer, PrintWriter>(Integer.getInteger(dirInfo.getOtherInfo()));
				break;
			case PersonalInfo:
				pwMap = new HashMap<Integer, PrintWriter>(1);
				break;
			default:
				break;
		}
		
		try {
			mFileDir = new File(mDirPath);
			if(!mFileDir.exists()) {
				if(!mFileDir.mkdir()) {
					mFileDir = null;
				}
			}
		}
		catch(Exception e) {
			mFileDir = null;
			Log.d(DEBUG_TAG, e.getLocalizedMessage());
		}
	}
	
	private PrintWriter createOrOpenTxtFile(String fileName) {
		if(mFileDir == null) {
			Toast.makeText(mContext, "txtDir is null ,failed to create log", Toast.LENGTH_LONG).show();
			return null;
		}
		
		File txtFile = new File(mFileDir, fileName);
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(txtFile,true));
		}
		catch(Exception e) {
			pw = null;
			Toast.makeText(mContext, "creating or openning txt file failed", Toast.LENGTH_LONG).show();
		}
		
		return pw;
	}
	
	public void appendLogWithNewline(int fileIndex, String data) {
		AppendLogTask task = new AppendLogTask(fileIndex, data);
		if(mThreadHandler == null) {
			initThreadAndHandler();
		}
		mThreadHandler.post(task);
	}
	
	//fileIndex is used for indexing in dictionary 
	public void createOrOpenLogFile(String fileName, int fileIndex) {
		PrintWriter pw = createOrOpenTxtFile(fileName);
		pwMap.put(fileIndex, pw);
	}
	
	public void closeFile(int fileIndex) {
		PrintWriter pw = pwMap.get(fileIndex);
		try {
			if(pw != null) {
				pw.close();
			}
		}
		catch(Exception e) {
			Log.d(DEBUG_TAG, e.getLocalizedMessage());
		}
	}
	
	
	private HandlerThread mThread = null;
	private Handler mThreadHandler = null;
	private void initThreadAndHandler() {
		mThread = new HandlerThread("FileWriterThread");
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper());
	}
	
	private class AppendLogTask implements Runnable {
		private String mData;
		private int mFileIndex;
		
		public AppendLogTask(int fileIndex, String data) {
			mFileIndex = fileIndex;
			mData = data;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				PrintWriter pw = pwMap.get(mFileIndex);
				if(pw != null) {
					pw.println(mData);
				}
				else {
					//calling this function in non UI thread maybe dangerous?
					Toast.makeText(mContext, "file haven't been successfully created or opened, writing file failed", Toast.LENGTH_SHORT).show();
				}
			}
			catch(Exception e) {
				Log.d(DEBUG_TAG,e.getLocalizedMessage());
			}
		}
		
	}
	
	
}
