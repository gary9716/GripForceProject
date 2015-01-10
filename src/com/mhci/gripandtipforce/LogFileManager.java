package com.mhci.gripandtipforce;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

public class LogFileManager {
	private final static String DEBUG_TAG = LogFileManager.class.getName();
	private final static String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GripForce/Logs";
	private static LogFileManager instance = null;
	private File logDir = null;
	private HandlerThread mThread = null;
	private Handler mThreadHandler = null;
	private Context mContext;
	
	public static LogFileManager getInstance(Context context,int numCharsInARow,int numRows) {
		if(instance == null) {
			synchronized (LogFileManager.class) {
				if(instance == null) {
					instance = new LogFileManager(numCharsInARow * numRows);
				}
			}
		}
		
		if(context != null) {
			instance.mContext = context;
		}
		
		return instance;
	}
	
	private LogFileManager(int numTotalChars) {
		initThreadAndHandler();
		pwMap = new HashMap<Integer, PrintWriter>(numTotalChars);
		funcIndex = FuncIndex.createOrOpenLogDir;
		mThreadHandler.post(taskForLoggerThread);
	}
	
	private enum FuncIndex {
		createOrOpenLogDir,
		createOrOpenLogFileForWriting,
		appendLogInNewline
	};
	
	private FuncIndex funcIndex = FuncIndex.createOrOpenLogDir;
	private String fileNameForLoggerThread = null;
	private String dataForLoggerThread = null;
	private int fileIndexForLoggerThread = 0; 
	
	private HashMap<Integer, PrintWriter> pwMap = null;
	
	public void appendLog(int fileIndex, String data) {
		fileIndexForLoggerThread = fileIndex;
		dataForLoggerThread = data;
		funcIndex = FuncIndex.appendLogInNewline;
		if(mThreadHandler == null) {
			initThreadAndHandler();
		}
		mThreadHandler.post(taskForLoggerThread);
	}
	
	//fileIndex is used for indexing in dictionary 
	public void createOrOpenLogFile(String fileName,int fileIndex) {
		fileNameForLoggerThread = fileName;
		fileIndexForLoggerThread = fileIndex;
		funcIndex = FuncIndex.createOrOpenLogFileForWriting;
		if(mThreadHandler == null) {
			initThreadAndHandler();
		}
		mThreadHandler.post(taskForLoggerThread);
	}
	
	private void initThreadAndHandler() {
		mThread = new HandlerThread("LoggerThread");
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper());
	}
	
	private Runnable taskForLoggerThread = new Runnable() {
		private void createOrOpenLogDir() {
			try {
				logDir = new File(dirPath);
				if(!logDir.exists()) {
					if(!logDir.mkdir()) {
						logDir = null;
					}
				}
			}
			catch(Exception e) {
				logDir = null;
				Log.d(DEBUG_TAG, e.getLocalizedMessage());
			}
			
		}
		
		private PrintWriter createOrOpenLog(String fileName) {
			if(logDir == null) {
				Toast.makeText(mContext, "logDir is null ,failed to create log", Toast.LENGTH_LONG).show();
				return null;
			}
			
			File logFile = new File(logDir, fileName);
			PrintWriter pw;
			try {
				pw = new PrintWriter(new FileWriter(logFile,true));
			}
			catch(Exception e) {
				pw = null;
				Toast.makeText(mContext, "creating or openning log file failed", Toast.LENGTH_LONG).show();
			}
			
			return pw;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			PrintWriter pw;
			switch(funcIndex) {
				case createOrOpenLogDir:
					createOrOpenLogDir();
					break;	
				case createOrOpenLogFileForWriting:
					pw = pwMap.get(fileIndexForLoggerThread);
					if(pw != null) {
						pw.close();
					}
					pw = createOrOpenLog(fileNameForLoggerThread);
					pwMap.put(fileIndexForLoggerThread, pw);
					break;
				case appendLogInNewline:
					try {
						pw = pwMap.get(fileIndexForLoggerThread);
						pw.println(dataForLoggerThread);
					}
					catch(Exception e) {
						Log.d(DEBUG_TAG,e.getLocalizedMessage());
					}
					break;
				default:
					Log.d(DEBUG_TAG, "unknown method for logger thread");
					break;
			}
			
		}
	};
	
	
}
