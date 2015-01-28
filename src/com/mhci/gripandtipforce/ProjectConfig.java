package com.mhci.gripandtipforce;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Collections;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class ProjectConfig {
	public final static String projectName = "GripForce"; 
	public static final UUID UUIDForBT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final int numBytesPerSensorStrip = 19;
	
	public static final String txtFileExtension = ".txt";
	
	public final static String rootDirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	public final static String exampleCharsDirName = "Example_Characters";
	public final static String exampleCharsFilesDirPath = rootDirPath +  "/" + exampleCharsDirName;
	
	public static String exampleCharsFileName(int grade) {
		return "Grade_" + grade + "_Characters" + txtFileExtension;
	}
	
	//SharePreference Keys
	
	//BT Settings
	public final static String Key_Preference_LastSelectedBT = "LastSelectedBT";
	public final static String Key_Preference_CurrentSelectedBTAddress = "CurrentSelectedBTAddress";
	
	//User Info
	public final static String Key_Preference_UserInfo = "UserInfo";
	public final static String Key_Preference_UserID = "UserID";
	public final static String Key_Preference_UserGrade = "UserGrade";
	public final static String Key_Preference_UserDominantHand = "UserDominantHand";
	
	public final static String leftHand = "left";
	public final static String rightHand = "right";
	public final static String defaultUserID = "DefaultUser";
	
	
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
