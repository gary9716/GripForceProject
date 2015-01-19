package com.mhci.gripandtipforce;

import java.io.File;

import android.os.Environment;

public class FileDirInfo {
	private FileType mFileType = FileType.Log;
	private static String[] defaultDirPath = new String[FileType.numFileType.ordinal()];
	private String mDirPath;
	private String mOtherInfo;
	
	public FileDirInfo(FileType fileType, String dirPath, String otherInfo) {
		if(defaultDirPath[0] == null) {
			String rootDirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
			defaultDirPath[FileType.Log.ordinal()] = rootDirPath + "/" + ProjectConfig.projectName + "/Logs";
			defaultDirPath[FileType.PersonalInfo.ordinal()] = rootDirPath + "/" + ProjectConfig.projectName + "/PersonalInformation";
			defaultDirPath[FileType.Image.ordinal()] = rootDirPath + "/" + ProjectConfig.projectName + "/Images";
		}
		
		mFileType = fileType;
		mOtherInfo = otherInfo;
		if(dirPath == null) {
			mDirPath = defaultDirPath[fileType.ordinal()];
		}
		else {
			mDirPath = dirPath;
		}
		
	}
	
	public void setFileType(FileType fileType) {
		mFileType = fileType;
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
	
}
