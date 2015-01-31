package com.mhci.gripandtipforce;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;

public class ProjectConfig {
	public final static boolean useSystemBarHideAndShow = true;
	
	public final static String projectName = "GripForce"; 
	public static final UUID UUIDForBT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final int numBytesPerSensorStrip = 19;
	
	public static final String txtFileExtension = ".txt";
	
	
	
	public final static String rootDirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	public final static String exampleCharsDirName = "Example_Characters";
	public final static String exampleCharsFilesDirPath = rootDirPath +  "/" + exampleCharsDirName;
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
	
	public static void initSomeVars(Context context) {
		Resources res = context.getResources();
		leftHand = res.getString(R.string.leftHand);
		rightHand = res.getString(R.string.rightHand);
		OneLine = res.getString(R.string.OneLine);
		SeparateChars = res.getString(R.string.SeparateChars);
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
	
	public final static int numOfGrades = 6;
	
	public static ArrayList<Integer> getTestingGradeSequence(int grade) {
		ArrayList<Integer> testingGrades = new ArrayList<Integer>();
		for(int i = 0;i < grade;i++) {
			testingGrades.add(Integer.valueOf(i+1));
		}
		
		Collections.shuffle(testingGrades);
		
		return testingGrades;
		
	}
	
}
