package com.mhci.gripandtipforce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

public class TxtFileManager extends FileManager{
	private final static String DEBUG_TAG = TxtFileManager.class.getName();
	private File mFileDir = null;
	private FileType mFileType = FileType.Log;
	private Context mContext;
	private PrintWriter[] pwArray;
	private LocalBroadcastManager mLBCManager = null;
	
	private void initArray(PrintWriter[] array) {
		for(PrintWriter pw : array) {
			pw = null;
		}
	}
	
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
				pwArray = new PrintWriter[(new Integer(dirInfo.getOtherInfo())).intValue() * 2];
				break;
			case PersonalInfo:
				pwArray = new PrintWriter[1];
				break;
			default:
				break;
		}
		
		initArray(pwArray);
		
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
		
		PrintWriter pw;
		try {
			//File txtFile = new File(mFileDir, getNonDuplicateFileName(mFileDir.getPath(), fileName));
			File txtFile = new File(mFileDir, fileName);
			if(!txtFile.exists()) {
				if(!txtFile.createNewFile()) {
					Toast.makeText(mContext, "creating txt file failed", Toast.LENGTH_LONG).show();
					return null;
				}
			}
			pw = new PrintWriter(new FileWriter(txtFile,true));
		}
		catch(Exception e) {
			pw = null;
			Toast.makeText(mContext, "creating or openning txt file failed", Toast.LENGTH_LONG).show();
		}
		
		return pw;
	}
	
	public void appendLogWithNewlineAsync(int charBoxIndex, String data) {
		AppendLogTask task = new AppendLogTask(charBoxIndex, data);
		mThreadHandler.post(task);
	}
	
	//fileIndex is used for indexing in dictionary 
	public boolean createOrOpenLogFile(String fileName, int charBoxIndex) {
		PrintWriter pw = createOrOpenTxtFile(fileName);
		if(pw == null) {
			return false;
		}
		if(charBoxIndex >= pwArray.length) {
			Log.d(debug_tag, "indexing out of bound");
			return false;
		}
		closeFile(charBoxIndex);
		pwArray[charBoxIndex] = pw;
		return true;
	}
	
	public boolean rearrangeIndices(Pair<Integer, Integer> toIndices,Pair<Integer, Integer> fromIndices) {
		int[] toIndicesArray = new int[]{toIndices.first,toIndices.second};
		int[] fromIndicesArray = new int[]{fromIndices.first,fromIndices.second};
		int numFromIndicesToMap = fromIndicesArray[1] - fromIndicesArray[0];
		if(toIndicesArray[1] - toIndicesArray[0] != numFromIndicesToMap) {
			return false;
		}
		for(int i = 0;i < numFromIndicesToMap;i++) {
			pwArray[toIndicesArray[0] + i] = pwArray[fromIndicesArray[0] + i];
			pwArray[fromIndicesArray[0] + i] = null;
			if(pwArray[toIndicesArray[0] + i] == null) {
				return false;
			}
		}
		return true;
		
	}
	
	private void closeFile(int fileIndex) {
		PrintWriter pw = pwArray[fileIndex];
		if(pw != null) {
			try {
				pw.close();
			}
			catch(Exception e) {
				Log.d(DEBUG_TAG, e.getLocalizedMessage());
			}
			pwArray[fileIndex] = null;
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
				pwArray[mFileIndex].println(mData);
//				PrintWriter pw = pwArray[mFileIndex];
				
//				if(pw != null) {
//					pw.println(mData);
//				}
//				else {
//					//calling this function in non UI thread maybe dangerous?
//					Toast.makeText(mContext, "file haven't been successfully created or opened, writing file failed", Toast.LENGTH_SHORT).show();
//				}
			}
			catch(Exception e) {
				Log.d(DEBUG_TAG,e.getLocalizedMessage());
			}
		}
		
	}
	
	public void toLoadChineseCharsAsync(int grade) {
		mThreadHandler.post(new LoadChineseCharsTask(grade));
	}
	
	public void toLoadChineseCharsSync(int grade) {
		(new LoadChineseCharsTask(grade)).run();
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
		
		String[] buffer = new String[container.size()];
		return container.toArray(buffer);
	}
	
	public static String getCharLogFileName(String userID, int grade, int charIndex) {
		return userID + "_" + grade + "_" + charIndex;
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		if(pwArray != null) {
			for(PrintWriter pw : pwArray) {
				try {
					pw.close();
				}
				catch(Exception e) {
					
				}
			}
		}
	}
	
}
