package com.mhci.gripandtipforce;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class InputDataActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inputdata);
		Button confirmButton = (Button)findViewById(R.id.button_confirm);
		final Context mContext = this;
		confirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//display an dialog to ask
				//save into a file and name according to some rules
				
				// 1. Instantiate an AlertDialog.Builder with its constructor
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

				// 2. Chain together various setter methods to set the dialog characteristics
				builder.setMessage("資料都確定填好且正確無誤了嗎？");

				// Add the buttons
				builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK button
						
						//add loading animation
						Intent intent = new Intent(mContext,ExperimentActivity.class);
						startActivity(intent);
					}
				});
				
				builder.setNegativeButton("還沒", null);

				// 3. Get the AlertDialog from create()
				(builder.create()).show();
				
			}
		});
		
	}
}
