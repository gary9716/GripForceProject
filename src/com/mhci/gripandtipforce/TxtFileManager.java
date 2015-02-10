package com.mhci.gripandtipforce;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
	private final static String DEBUG_TAG = "TxtFileManager";
	private File mFileDir = null;
	private FileType mFileType = FileType.Log;
	private Context mContext;
	private BufferedWriter[] writerArray;
	private LocalBroadcastManager mLBCManager = null;
	
	private void initArray(BufferedWriter[] array) {
		for(BufferedWriter writer : array) {
			writer = null;
		}
	}
	
	public TxtFileManager(FileDirInfo dirInfo,Context context) {
		super(context, dirInfo.getFileType());
		readUserConfig();
		
//		if(!FileDirInfo.isExternalStorageWritable()) {
//			Toast.makeText(context, "資料無法寫入指定資料夾,請再次確認設定無誤", Toast.LENGTH_LONG).show();
//		}
		
		mContext = context;
		mFileType = dirInfo.getFileType();
		initThreadAndHandler();
		
		mLBCManager = LocalBroadcastManager.getInstance(mContext);
		
		switch(mFileType) {
			case Log:
				int numOfInstanceToAlloc = 1;
				if(dirInfo.getOtherInfo() != null) {
					numOfInstanceToAlloc = (new Integer(dirInfo.getOtherInfo())).intValue();
				}
				writerArray = new BufferedWriter[numOfInstanceToAlloc];
				break;
			case PersonalInfo:
				writerArray = new BufferedWriter[1];
				break;
			default:
				break;
		}
		
		initArray(writerArray);
		mFileDir = new File(dirInfo.getDirPath());
		
	}
	
	private BufferedWriter createOrOpenTxtFile(String fileName) {
		if(mFileDir == null) {
			Toast.makeText(mContext, "txtDir is null ,failed to create log", Toast.LENGTH_LONG).show();
			return null;
		}
		
		BufferedWriter writer;
		try {
			//File txtFile = new File(mFileDir, getNonDuplicateFileName(mFileDir.getPath(), fileName));
			File txtFile = new File(mFileDir, fileName);
			if(!txtFile.exists()) {
				if(!txtFile.createNewFile()) {
					Toast.makeText(mContext, "creating txt file failed", Toast.LENGTH_LONG).show();
					return null;
				}
			}
			writer = new BufferedWriter(new FileWriter(txtFile,true));
		}
		catch(Exception e) {
			writer = null;
			Toast.makeText(mContext, "creating or openning txt file failed", Toast.LENGTH_LONG).show();
		}
		
		return writer;
	}
	
	/*
	public void appendLogWithNewlineAsync(int arrayIndex, String data) {
		AppendLogTask task = new AppendLogTask(arrayIndex, data);
		Log.d(debug_tag, "it's going to append data in index:" + arrayIndex + ",data:" + data);
		mThreadHandler.post(task);
	}
	*/
	
	
	public void appendLogWithNewlineSync(int arrayIndex, String data) {
		// TODO Auto-generated method stub
		BufferedWriter writer = writerArray[arrayIndex];
		if(writer == null) {
			return;
		}
		try {
			writer.write(data);
			writer.newLine();
			//writerArray[arrayIndex].flush();
		}
		catch(Exception e) {
			Log.d(debug_tag,"exception in AppendLogTask,e:" + e.getLocalizedMessage());
			//Log.d(DEBUG_TAG,e.getLocalizedMessage());
		}
		//Log.d(debug_tag,"done append log");
		
	}
	
	//fileIndex is used for indexing in dictionary 
	public boolean createOrOpenLogFileSync(String fileName, int arrayIndex) {
		if(arrayIndex >= writerArray.length) {
			Log.d(debug_tag, "indexing out of bound in createOrOpenLogFileSync");
			return false;
		}
		
		BufferedWriter writer = createOrOpenTxtFile(fileName);
		if(writer == null) {
			return false;
		}
		
//		if(!pw.equals(pwArray[arrayIndex])) {
//			try {
//				pwArray[arrayIndex].close();
//			}
//			catch(Exception e) {
//				
//			}
//		}
		
		closeFile(arrayIndex);
		writerArray[arrayIndex] = writer;
		return true;
	}
	
	//don't forget to close files before call this function
	public boolean rearrangeIndices(Pair<Integer, Integer> toIndices,Pair<Integer, Integer> fromIndices) {
		int[] toIndicesArray = new int[]{toIndices.first,toIndices.second};
		int[] fromIndicesArray = new int[]{fromIndices.first,fromIndices.second};
		int numFromIndicesToMap = fromIndicesArray[1] - fromIndicesArray[0];
		if(toIndicesArray[1] - toIndicesArray[0] != numFromIndicesToMap) {
			return false;
		}
		for(int i = 0;i < numFromIndicesToMap;i++) {
			int toIndex = toIndicesArray[0] + i;
			int fromIndex = fromIndicesArray[0] + i;
			writerArray[toIndex] = writerArray[fromIndex];
			writerArray[fromIndex] = null;
			if(writerArray[toIndex] == null) {
				return false;
			}
		}
		return true;
		
	}
	
	public void closeFile(int arrayIndex) {
		BufferedWriter writer = writerArray[arrayIndex];
		if(writer != null) {
			try {
				writer.flush();
				writer.close();
			}
			catch(Exception e) {
				Log.d(DEBUG_TAG, e.getLocalizedMessage());
			}
			writerArray[arrayIndex] = null;
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
		private int mArrayIndex;
		
		public AppendLogTask(int arrayIndex, String data) {
			mArrayIndex = arrayIndex;
			mData = data;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				writerArray[mArrayIndex].write(mData);
				writerArray[mArrayIndex].newLine();
			}
			catch(Exception e) {
				Log.d(DEBUG_TAG,"exception in AppendLogTask,e:" + e.getLocalizedMessage());
				//Log.d(DEBUG_TAG,e.getLocalizedMessage());
			}
		}
		
	}
	
	public void toLoadChineseCharsAsync(int grade) {
		mThreadHandler.post(new LoadChineseCharsTask(grade));
	}
	
	public void toLoadChineseCharsSync(int grade) {
		(new LoadChineseCharsTask(grade)).run();
	}
	
	public Runnable getLoadChineseCharTask(int grade) {
		return (new LoadChineseCharsTask(grade));
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
		
//		if(!FileDirInfo.isExternalStorageReadable()) {
//			Toast.makeText(mContext, "無法讀取指定資料夾的範例文字,請再次確認設定無誤", Toast.LENGTH_LONG).show();
//			return null;
//		}
		
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
		//Log.d(debug_tag,"numCharsHaveBeenLoaded:" + buffer.length + ",grade:" + grade);
		return container.toArray(buffer);
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		if(writerArray != null) {
			for(int i = 0;i < writerArray.length;i++) {
				closeFile(i);
			}
		}
	}
	
}
