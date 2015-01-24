package com.mhci.gripandtipforce;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BluetoothSettingActivity extends Activity{
	private Context mContext = null;
	private BluetoothManager btManager;
	private PopupwinForSelectingDeviceUsingAlertDialog popwin = null;
	private TextView mCurrentState = null;
	private TextView mLastConnection = null;
	private TextView mCurrentSelectedBT = null;
	
	//data
	private String mCurrentSelectedBTName = null;
	private String mCurrentSelectedBTAddress = null;
	
	private BTDeviceInfoAdapter mDisplayAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bt_setting);
		mContext = this;
		mDisplayAdapter = new BTDeviceInfoAdapter(this, android.R.layout.simple_list_item_1);
		
		popwin = new PopupwinForSelectingDeviceUsingAlertDialog(mContext);
		btManager = new BluetoothManager(this, btEventReceiver, mDisplayAdapter);
		
		mCurrentSelectedBT = (TextView)findViewById(R.id.text_currently_selected_device);
		mLastConnection = (TextView)findViewById(R.id.text_lastly_selected_device);
		mCurrentState = (TextView)findViewById(R.id.text_bt_state);
		
		mCurrentSelectedBT.addTextChangedListener(textChangedListener);
		mCurrentState.addTextChangedListener(textChangedListener);
		
		((Button)findViewById(R.id.button_select_from_bonded_devices)).setOnClickListener(buttonListener);
		((Button)findViewById(R.id.button_select_from_discovered_devices)).setOnClickListener(buttonListener);
		((Button)findViewById(R.id.button_next_page)).setOnClickListener(buttonListener);
		((Button)findViewById(R.id.button_connect)).setOnClickListener(buttonListener);
		
	}
	
	private TextWatcher textChangedListener = new TextWatcher() {
		
		private final static String debug_tag = "textWatcherTest";
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			Log.d(debug_tag, "onTextChanged:" + s.toString() + ",start:" + start + ",before:" + before + ",count:" + count);
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			Log.d(debug_tag, "beforeTextChanged:" + s.toString() + ",start:" + start + ",count:" + count);
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			Log.d(debug_tag, "afterTextChanged:" + s.toString());
		}
	};
	
	private void resetDataContainer() {
		mCurrentSelectedBTName = null;
		mCurrentSelectedBTAddress = null;
	}
	
	private OnClickListener buttonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getId() == R.id.button_select_from_bonded_devices) {
				//popwin = new PopupwinForSelectingDevice(mContext);
				resetDataContainer();
				popwin.show();
				btManager.getBondedDevicesInfoAndUpdate();
			}
			else if(v.getId() == R.id.button_select_from_discovered_devices) {
				//popwin = new PopupwinForSelectingDevice(mContext);
				resetDataContainer();
				popwin.show();
				popwin.showProgressBar(true);
				btManager.startDiscovering();
			}
			else if(v.getId() == R.id.button_connect) {
				
			}
			else if(v.getId() == R.id.button_next_page) {
				
			}
			
		}
	};
	
	private BroadcastReceiver btEventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
	        if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	        		if(popwin != null) {
	        			popwin.showProgressBar(false);
	        		}
	        }
		}
	};
	
	
	private class PopupwinForSelectingDeviceUsingAlertDialog {
		public final static String debug_tag = "alertDialogForSelectingBTDevice";
		
		private AlertDialog mPopwin;
		private ProgressBar progressBarLoadingBTDevices;
		private ListView mListView;
		private View mPopwinView;
		public PopupwinForSelectingDeviceUsingAlertDialog(Context context) {
			
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mPopwinView = inflater.inflate(R.layout.alertdialog_selection, null);
			
			mListView = (ListView)mPopwinView.findViewById(R.id.list_btdevicesinfo);
			mListView.setAdapter(mDisplayAdapter);
			mListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> adapterView, View clickedItemView,
						int pos, long id) {
					//Log.d(debug_tag, "listen!!");
					// TODO Auto-generated method stub
					
					mDisplayAdapter.setSelectedIndex(pos);
					
				}
			});
			
			progressBarLoadingBTDevices = (ProgressBar)mPopwinView.findViewById(R.id.progressbar_loading_progress);
			progressBarLoadingBTDevices.setIndeterminate(true);
			progressBarLoadingBTDevices.setVisibility(View.GONE);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("藍芽裝置列表");
			builder.setView(mPopwinView);
			builder.setNegativeButton("不選", btnListener);
			builder.setPositiveButton("選擇", btnListener);
			
			mPopwin = builder.create();
			mPopwin.setCanceledOnTouchOutside(true);
			mPopwin.setOnCancelListener(new OnCancelListener() {	
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					actionTakenAfterWindowDismissed();
				}
			});
			
			mPopwin.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					// TODO Auto-generated method stub
					actionTakenAfterWindowDismissed();
				}
			});
		}
		
		private void actionTakenAfterWindowDismissed() {
			showProgressBar(!progressBarLoadingBTDevices.isShown());
			btManager.stopDiscovering();
			mDisplayAdapter.setSelectedIndex(-1);
		}
		
		public void show() {
			mPopwin.show();
		}
		
		public void dismiss() {
			mPopwin.dismiss();
		}
		
		public void showProgressBar(boolean toBeVisible) {
			if(toBeVisible) {
				progressBarLoadingBTDevices.setVisibility(View.VISIBLE);
			}
			else {
				progressBarLoadingBTDevices.setVisibility(View.GONE);
			}
		}
		
		private DialogInterface.OnClickListener btnListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(which == dialog.BUTTON_POSITIVE) {
					//Log.d(debug_tag, "pos:" + mDisplayAdapter.getSelectedIndex());
					int currentSelectedIndex = mDisplayAdapter.getSelectedIndex();
					if(currentSelectedIndex != -1) {
						String data = mDisplayAdapter.getItem(currentSelectedIndex);
						String[] splitedData = data.split("\n");
						if(splitedData != null && splitedData.length >= 2) {
							mCurrentSelectedBTName = splitedData[0];
							mCurrentSelectedBT.setText(mCurrentSelectedBTName);
							mCurrentSelectedBTAddress = splitedData[1];
						}
						else {
							Log.d(debug_tag, "failed to parse data in AlertDialog for selecting bt device");
						}
						dismiss();
					}
				}
				else if(which == dialog.BUTTON_NEGATIVE){
					dismiss();
				}
			}
		};
		
	}
	
}
