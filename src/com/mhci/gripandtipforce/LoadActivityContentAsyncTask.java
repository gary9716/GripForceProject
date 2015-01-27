package com.mhci.gripandtipforce;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

public class LoadActivityContentAsyncTask extends AsyncTask<Void, Integer, Void> {

	private Context mContext = null;
	private ProgressDialog progressDialog;
	private Runnable mTaskToRunInBG;
	private Runnable mPostTaskAfterBGTask;
	private final static String dialogTitle = "讀取中...";
	private final static String dialogMessage = "下一個畫面需要些讀取時間,請稍候";
	
	public LoadActivityContentAsyncTask(Context context,Runnable taskToRunInBG,Runnable postTaskAfterBGTask) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mTaskToRunInBG = taskToRunInBG;
		mPostTaskAfterBGTask = postTaskAfterBGTask;
		
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		progressDialog = ProgressDialog.show(mContext,dialogTitle,dialogMessage, true, false);
		
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
