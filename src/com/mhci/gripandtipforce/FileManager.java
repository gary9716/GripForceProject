package com.mhci.gripandtipforce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;

public class FileManager {
	public final static String debug_tag = "CustomizedFileManager";
	private final static String userFileConfigsPostfixname = "_userFileConfig";
	private File internalDir = null; 
	private HashMap<String, Integer> mUserFileConfig = null;
	private String mUserID;
	private FileType mFileType;
	
	public FileManager(Context context, FileType fileType) {
		internalDir = context.getDir("data", Context.MODE_PRIVATE);
		mUserID = ProjectConfig.getCurrentUserID(context);
		mFileType = fileType;
	}
	
	private String getConfigFileName(String userID, FileType fileType) {
		return userID + "_" + fileType.name() + userFileConfigsPostfixname;
	}
	
	protected void readUserConfig() {
		File configFile = new File(internalDir, getConfigFileName(mUserID, mFileType));
		if(!configFile.exists()) {
			createUserConfig(configFile);
			mUserFileConfig = new HashMap<String, Integer>();
			return;
		}
		else {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(configFile));
				mUserFileConfig = (HashMap<String, Integer>) ois.readObject();
				ois.close();
			}
			catch(Exception e) {
				//Log.d(debug_tag, e.getLocalizedMessage());
			}
			return;
		}
	}
	
	private void createUserConfig(File file) {
		try {
			if(!file.createNewFile()) {
				Log.d(debug_tag, "user config file creation failed");
			}
		}
		catch(Exception e) {
			Log.d(debug_tag, e.getLocalizedMessage());
		}
	}
	
	protected void saveUserConfig() {
		File configFile = new File(internalDir, getConfigFileName(mUserID, mFileType));    
		if(!configFile.exists()) {
			createUserConfig(configFile);
		}
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(configFile));
			outputStream.writeObject(mUserFileConfig);
			outputStream.close();
		}
		catch(Exception e) {
			Log.d(debug_tag, e.getLocalizedMessage());
		}
	}
	
	protected String getNonDuplicateFileName(String dirPath, String fileNameToSave) {
		return dirPath + "/" + fileNameToSave + "_" + getNextFileIndex(fileNameToSave);
	}
	
	protected int getNextFileIndex(String fileNamePrefix) {
		Integer valObj = mUserFileConfig.get(fileNamePrefix);
		int nextIndex = 0;
		if(valObj != null) {
			nextIndex = valObj.intValue() + 1;
		}
		else {
			nextIndex = 1;
		}
		
		//update current index
		mUserFileConfig.put(fileNamePrefix, nextIndex);
		
		return nextIndex;
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		if(mUserFileConfig != null) {
			saveUserConfig();
		}
	}
	
	
}
