package com.mhci.gripandtipforce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class TxtFileManager extends FileManager{
	private final static String DEBUG_TAG = TxtFileManager.class.getName();
	private File mFileDir = null;
	private FileType mFileType = FileType.Log;
	private Context mContext;
	private HashMap<Integer, PrintWriter> pwMap = null;
	private LocalBroadcastManager mLBCManager = null;
	
	public TxtFileManager(FileDirInfo dirInfo,Context context) {
		super(context, dirInfo.getFileType());
		readUserConfig();
		
		if(!FileDirInfo.isExternalStorageWritable()) {
			Toast.makeText(context, "資料無法寫入指定資料夾,請再次確認設定無誤", Toast.LENGTH_LONG).show();
		}
		
		mContext = context;
		mFileType = dirInfo.getFileType();
		initThreadAndHandler();
		
		mLBCManager = LocalBroadcastManager.getInstance(mContext);
		
		switch(mFileType) {
			case Log:
				pwMap = new HashMap<Integer, PrintWriter>((new Integer(dirInfo.getOtherInfo())).intValue());
				break;
			case PersonalInfo:
				pwMap = new HashMap<Integer, PrintWriter>(1);
				break;
			default:
				break;
		}
		
		try {
			mFileDir = new File(dirInfo.getDirPath());
			if(!mFileDir.exists()) {
				if(!mFileDir.mkdir()) {
					mFileDir = null;
					Log.d(debug_tag, "mkdir failed in TxtFileManager");
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
		
		File txtFile = new File(mFileDir, getNonDuplicateFileName(mFileDir.getPath(), fileName));
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
	
	public void toLoadChineseChars(int grade) {
		mThreadHandler.post(new LoadChineseCharsTask(grade));
	}
	
	private class LoadChineseCharsTask implements Runnable {
		
		private int mGrade;
		
		public LoadChineseCharsTask(int grade) {
			// TODO Auto-generated constructor stub
			mGrade = grade;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String[] result = loadChineseCharsDependOnGrade(mGrade);
			if(result != null) {
				Intent intent = new Intent(ExperimentActivity.Action_update_chars);
				intent.putExtra(ExperimentActivity.Key_ExChars, result);
				mLBCManager.sendBroadcast(intent);
			}
		}
		
	}
	
	private String[] loadChineseCharsDependOnGrade(int grade) {
		
		if(!FileDirInfo.isExternalStorageReadable()) {
			Toast.makeText(mContext, "無法讀取指定資料夾的範例文字,請再次確認設定無誤", Toast.LENGTH_LONG).show();
			return null;
		}
		
		File exampleCharsFile = new File(ProjectConfig.exampleCharsFilesDirPath, ProjectConfig.exampleCharsFileName(grade));
		
		if(!exampleCharsFile.exists()) {
			Toast.makeText(mContext, "找不到" + grade + "年級的範例文字,請再次確認檔案已放到正確的資料夾底下" , Toast.LENGTH_LONG).show();
			return null;
		}
		
		ArrayList<String> container = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(exampleCharsFile), "UTF-8"));
			while(true) {
				String singleChar = reader.readLine();
				if(singleChar != null) {
					container.add(singleChar);
				}
				else {
					break;
				}
				
				//maybe we'll need to load partially and send the chars to show on UI.
				//we can use broadcast mechanism.
			}
		}
		catch(Exception e) {
			Log.d(debug_tag, e.getLocalizedMessage());
		}
		finally {
			if(reader != null) {
				try {
					reader.close();
				}
				catch(Exception e) { 
					
				}
			}
		}
		
		return (String [])container.toArray();
	}
	
	
}
