package com.mhci.gripandtipforce;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class LogFileManager {
	private final static String DEBUG_TAG = LogFileManager.class.getName();
	private final static String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SPen/images";
	private static LogFileManager instance = null;
	private File logDir = null;
	
	
	public static LogFileManager getInstance() {
		
		return null;
	}
	
	private File createLog(String fileName) {
		return null;
	}
	
	
}
