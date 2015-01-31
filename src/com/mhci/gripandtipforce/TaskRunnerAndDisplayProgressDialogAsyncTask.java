package com.mhci.gripandtipforce;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

public class TaskRunnerAndDisplayProgressDialogAsyncTask extends AsyncTask<Void, Integer, Void> {
	public final static String startAsyncTask = TaskRunnerAndDisplayProgressDialogAsyncTask.class.getName() + ".start";
	public final static String stopAsyncTask = TaskRunnerAndDisplayProgressDialogAsyncTask.class.getName() + ".stop";
	public final static String Key_title = "Title";
	public final static String Key_msg = "Msg";
	
	private Context mContext = null;
	private ProgressDialog progressDialog;
	private Runnable mTaskToRunInBG;
	private Runnable mPostTaskAfterBGTask;
	private final static String defaultDialogTitle = "處理中...";
	private final static String defaultDialogMessage = "系統忙碌中,請稍候";
	private String userDefinedTitle = null;
	private String userDefinedMsg = null;
	
	public TaskRunnerAndDisplayProgressDialogAsyncTask(Context context,Runnable taskToRunInBG,Runnable postTaskAfterBGTask) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mTaskToRunInBG = taskToRunInBG;
		mPostTaskAfterBGTask = postTaskAfterBGTask;	
	}
	
	public TaskRunnerAndDisplayProgressDialogAsyncTask(Context context,Runnable taskToRunInBG,Runnable postTaskAfterBGTask,String title,String msg) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mTaskToRunInBG = taskToRunInBG;
		mPostTaskAfterBGTask = postTaskAfterBGTask;
		userDefinedMsg = msg;
		userDefinedTitle = title;
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		String msg;
		if(userDefinedMsg != null) {
			msg = userDefinedMsg;
		}
		else {
			msg = defaultDialogMessage;
		}
		String title;
		if(userDefinedTitle != null) {
			title = userDefinedTitle;
		}
		else {
			title = defaultDialogTitle;
		}
		
		progressDialog = ProgressDialog.show(mContext,title,msg, true, false);
		
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		if(mTaskToRunInBG != null) {
			mTaskToRunInBG.run();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if(mPostTaskAfterBGTask != null) {
			mPostTaskAfterBGTask.run();
		}
		progressDialog.dismiss();
		
	}

}
