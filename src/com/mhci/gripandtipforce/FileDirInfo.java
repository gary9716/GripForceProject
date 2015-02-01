package com.mhci.gripandtipforce;


public class FileDirInfo {
	private FileType mFileType = FileType.Log;
	public static String[] _defaultDirPath = null;
	private String mDirPath;
	private String mOtherInfo;
	
	public FileDirInfo(FileType fileType, String dirPath, String otherInfo) {
		
		mFileType = fileType;
		mOtherInfo = otherInfo;
		if(dirPath == null) {
			mDirPath = _defaultDirPath[fileType.ordinal()];
		}
		else {
			mDirPath = dirPath;
		}
		
	}
	
	public void setFileType(FileType fileType, boolean toUseDefaultDirPath) {
		mFileType = fileType;
		if(toUseDefaultDirPath) {
			mDirPath = _defaultDirPath[fileType.ordinal()];
		}
	}
	
	public void setDirPath(String dirPath) {
		mDirPath = dirPath;
	}
	
	public void setOtherInfo(String otherInfo) {
		mOtherInfo = otherInfo;
	}
	
	public FileType getFileType() {
		return mFileType;
	}
	
	public String getDirPath() {
		return mDirPath;
	}
	
	public String getOtherInfo() {
		return mOtherInfo;
	}
	
	/*
	
	public static boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	public static boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	*/
}
