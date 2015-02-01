package com.mhci.gripandtipforce;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Collections;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class ProjectConfig {
	public final static String debug_tag = "ProjectConfig";
	
	public final static String projectName = "GripForce"; 
	public final static boolean useSystemBarHideAndShow = true;
	public final static boolean useRealSDCard = true;
	public static final int numBytesPerSensorStrip = 19;
	public static final int numSensorStrips = 6;
	
	public final static float inchPerCM = 0.393700787f;
	public static final UUID UUIDForBT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final String txtFileExtension = ".txt";
	public static final String imgFileExtension = ".png";
	
	public static String writableRootPath = null;
	public final static String internalSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	private final static String externalStorageName = "extSdCard";
	public final static String externalSDCardPath = tryToFindExtSDCardPath();
	
	private static String tryToFindExtSDCardPath() {
		int lastIndex = internalSDCardPath.lastIndexOf('/');
		String path = internalSDCardPath.substring(0, lastIndex + 1) + externalStorageName;
		try {
			File dir = new File(path);
			if(dir.exists()) {
				return path;
			}
			else {
				return "/storage/" + externalStorageName;
			}
		}
		catch(Exception e) {
			return "/storage/" + externalStorageName;
		}
	}
	
	public static String getRootDirPath() {
		if(writableRootPath == null) {
			setWritableRootPath(null);
		}
		return writableRootPath;
	}
	
	public static void setWritableRootPath(Context context) {
		writableRootPath = null;
		
		String defaultPathToUse = null;
		if(useRealSDCard) {
			defaultPathToUse = externalSDCardPath;
		}
		else {
			defaultPathToUse = internalSDCardPath;
		}
		
		if(!isDirPathWritable(defaultPathToUse)) {
			if(externalSDCardPath.equals(defaultPathToUse)) {
				Toast.makeText(context, "SD卡無法使用,請確認是否正確安裝", Toast.LENGTH_LONG).show();
			}
			defaultPathToUse = internalSDCardPath;
			if(!isDirPathWritable(defaultPathToUse)) {
				Log.d(debug_tag, "both storage are not available");
				if(context != null) {
					Toast.makeText(context, "所有空間目前都無法使用,請確認是否與電腦斷開連線或SD卡正確安裝", Toast.LENGTH_LONG).show();
				}
				return;
			}
		}
		
		writableRootPath = defaultPathToUse;
		
		if(context != null) {
			//Toast.makeText(context, "path become " + writableRootPath, Toast.LENGTH_LONG).show();
			if(externalSDCardPath.equals(writableRootPath)) {
				Toast.makeText(context, "使用SD卡中", Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(context, "使用內部儲存空間", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	public final static String exampleCharsDirName = "Example_Characters";
	public static String exampleCharsFilesDirPath = null;
	public final static String gripForceLogPrefix = "GripForce_";
	public final static String tipForceLogPrefix = "TipForce_";
	public final static String restartMark = "restart_point";
	
	public static String exampleCharsFileName(int grade) {
		return "Grade_" + grade + "_Characters" + txtFileExtension;
	}
	
	//SharePreference Keys
	
	//BT Settings
	public final static String Key_Preference_LastSelectedBT = "LastSelectedBT";
	public final static String Key_Preference_CurrentSelectedBTAddress = "CurrentSelectedBTAddress";
	
	//User Info
	public final static String userInfo_delimiter = "\n";
	public final static String Key_Preference_UserInfo = "UserInfo";
	public final static String Key_Preference_UserID = "UserID";
	public final static String Key_Preference_UserGrade = "UserGrade";
	public final static String Key_Preference_UserDominantHand = "UserDominantHand";
	
	public static String leftHand = null;
	public static String rightHand = null;
	public final static String defaultUserID = "DefaultUser";
	
	//Experiment Setting
	public static final String Key_Preference_ExperimentSetting = "expSetting";
	public static final String Key_Preference_TestingLayout = "TestingLayout";
	
	public static String OneLine = null;
	public static String SeparateChars = null;

	public final static int numOfGrades = 6;
	
	public static void initSomeVars(Context context) {
		Resources res = context.getResources();
		leftHand = res.getString(R.string.leftHand);
		rightHand = res.getString(R.string.rightHand);
		OneLine = res.getString(R.string.OneLine);
		SeparateChars = res.getString(R.string.SeparateChars);
		setWritableRootPath(context);
		FileDirInfo._defaultDirPath = setDefaultDirPaths(context);
		
		
	}
	
	private final static String testPathWritable = "testPathWritable";
	
	public static boolean isDirPathWritable(String dirPath) {
		try {
			if(dirPath == null) {
				return false;
			}
			File dir = new File(dirPath);
			if(!dir.exists()) {
				if(!dir.mkdirs()) {
					Log.d(testPathWritable, "cannot create dir");
					return false;
				}
				else {
					tryToWriteTempFile(dir);
					return true;
				}
			}
			else {
				if(dir.isDirectory()) {
					tryToWriteTempFile(dir);
					return true;
				}
				else {
					Log.d(testPathWritable, "it's not a dir");
					return false;
				}
			}
			
		}
		catch(Exception e) {
			Log.d(testPathWritable, e.getLocalizedMessage());
			return false;
		}
	}
	
	public static String getCurrentUserID(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(Key_Preference_UserInfo, Context.MODE_PRIVATE);
		if(preferences != null) {
			return preferences.getString(Key_Preference_UserID, defaultUserID);
		}
		else {
			return defaultUserID;
		}
	}
	
	public static ArrayList<Integer> getTestingGradeSequence(int grade) {
		ArrayList<Integer> testingGrades = new ArrayList<Integer>();
		for(int i = 0;i < grade;i++) {
			testingGrades.add(Integer.valueOf(i+1));
		}
		
		Collections.shuffle(testingGrades);
		
		return testingGrades;
	}

	private static String[] setDefaultDirPaths(Context context) {
		String[] defaultPaths = new String[FileType.numFileType.ordinal()];
		String rootPath = getRootDirPath();
		
		defaultPaths[FileType.Log.ordinal()] = rootPath + "/" + projectName + "/Logs";
		defaultPaths[FileType.PersonalInfo.ordinal()] = rootPath + "/" + projectName + "/PersonalInformation";
		defaultPaths[FileType.Image.ordinal()] = rootPath + "/" + projectName + "/Images";
		defaultPaths[FileType.ExampleChars.ordinal()] = rootPath + "/" + projectName + "/" + exampleCharsDirName;
		exampleCharsFilesDirPath = defaultPaths[FileType.ExampleChars.ordinal()];
		checkDirExistence(defaultPaths,context);
		
		return defaultPaths;
	}
	
	private static void checkDirExistence(String[] defaultPaths,Context context) {
		for(String path : defaultPaths) {
			try {
				File dir = new File(path);
				if(!dir.exists()) {
					if(!dir.mkdirs()) {
						Toast.makeText(context, path + " is not creatable", Toast.LENGTH_SHORT).show();
						Log.d(debug_tag, path + " is not creatable");
					}
				}
			}
			catch(Exception e) {
				
			}
		}
	}
	
	private static void tryToWriteTempFile(File dir) throws Exception {
		//try to save an empty file
		File tmpFile = File.createTempFile("tryToWrite", null, dir);
		tmpFile.delete();
	}
	
}
