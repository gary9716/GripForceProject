package com.mhci.gripandtipforce;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.Toast;

public class BluetoothSettingActivity extends Activity{
	private final static String emptyStateName = "無";
	
	private Context mContext = null;
	private LocalBroadcastManager mLBCManager;
	private BluetoothManager btManager;
	private TextView mLastSelectedBT = null;
	private TextView mCurrentSelectedBT = null;
	//private Button testConnectionButton = null;
	private Button nextPageButton = null;
	
	//data
	private String mCurrentSelectedBTName = null;
	private String mCurrentSelectedBTAddress = null;
	
	private PopupwinForSelectingDeviceUsingAlertDialog popwin = null;
	private BTDeviceInfoAdapter mDisplayAdapter;
	
	private SharedPreferences mBTSettings;
	private BluetoothManager mBTManager;
	
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
		mLastSelectedBT = (TextView)findViewById(R.id.text_lastly_selected_device);
		
		mLastSelectedBT.addTextChangedListener(textChangedListener);
		mCurrentSelectedBT.addTextChangedListener(textChangedListener);
		
		((Button)findViewById(R.id.button_select_from_bonded_devices)).setOnClickListener(buttonListener);
		((Button)findViewById(R.id.button_select_from_discovered_devices)).setOnClickListener(buttonListener);
		nextPageButton = (Button)findViewById(R.id.button_experiment_next_step);
		nextPageButton.setOnClickListener(buttonListener);
		//testConnectionButton = (Button)findViewById(R.id.button_test_connection);
		//testConnectionButton.setOnClickListener(buttonListener);
		
		IntentFilter intentFilter = new IntentFilter(BluetoothClientConnector.Msg_update_info);
		mLBCManager = LocalBroadcastManager.getInstance(mContext);
		mLBCManager.registerReceiver(infoReceiver, intentFilter);
		
		mBTManager = new BluetoothManager(mContext, null, null);
		
		mBTSettings = getSharedPreferences("BTSettings", Context.MODE_PRIVATE);
		mLastSelectedBT.setText(mBTSettings.getString(ProjectConfig.Key_Preference_LastSelectedBT, emptyStateName));
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mLBCManager != null) {
			mLBCManager.unregisterReceiver(infoReceiver);
		}
	}
	
	private TextWatcher textChangedListener = new TextWatcher() {
		
		private final static String debug_tag = "textWatcherTest";
		
		private void setButtonsVisibility(boolean toShow) {
			if(toShow) {
				nextPageButton.setVisibility(View.VISIBLE);
				//testConnectionButton.setVisibility(View.VISIBLE);
			}
			else {
				nextPageButton.setVisibility(View.GONE);
				//testConnectionButton.setVisibility(View.GONE);
			}
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			//Log.d(debug_tag, "onTextChanged:" + s.toString() + ",start:" + start + ",before:" + before + ",count:" + count);
			if(!canGoToNextPage()) {
				setButtonsVisibility(false);
			}
			else {
				setButtonsVisibility(true);
			}	
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			//Log.d(debug_tag, "beforeTextChanged:" + s.toString() + ",start:" + start + ",count:" + count);
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			//Log.d(debug_tag, "afterTextChanged:" + s.toString());
		}
	};
	
	private void setBTName(String name) {
		mCurrentSelectedBTName = name;
		if(name == null) {
			mCurrentSelectedBT.setText(emptyStateName);
		}
		else {
			mCurrentSelectedBT.setText(name);
		}
	}
	
	private void setBTNameAndAddress(String name, String addr) {
		setBTName(name);
		mCurrentSelectedBTAddress = addr;
	}
	
	private void resetDataContainer() {
		setBTNameAndAddress(null, null);
	}
	
	private boolean canGoToNextPage() {
		return !(mLastSelectedBT.getText().toString().equals(emptyStateName) && mCurrentSelectedBT.getText().toString().equals(emptyStateName));
	}
	
	private View viewBeUnclickable = null;
	
	private void setViewUnclickable(View v) {
		v.setClickable(false);
		viewBeUnclickable = v;
	}
	
	private OnClickListener buttonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getId() == R.id.button_select_from_bonded_devices) {
				setViewUnclickable(v);
				btManager.getBondedDevicesInfoAndUpdate();
				popwin.show();
				
			}
			else if(v.getId() == R.id.button_select_from_discovered_devices) {
				setViewUnclickable(v);
				btManager.startDiscovering();
				popwin.show();
				popwin.showProgressBar(true);
			}
			/* deprecated 
			else if(v.getId() == R.id.button_test_connection) {
				if(mCurrentSelectedBTAddress != null) {
					Intent actionIntent = new Intent(mContext, BluetoothClientConnector.class);
					actionIntent.setAction(BluetoothClientConnector.Action_test_connection);
					actionIntent.putExtra(BluetoothClientConnector.DeviceAddrKey, mCurrentSelectedBTAddress);
					startService(actionIntent);
				}
				else {
					Toast.makeText(BluetoothSettingActivity.this, "please reselect the bluetooth device you wanted to connect", Toast.LENGTH_SHORT).show();
				}
			}
			*/
			else if(v.getId() == R.id.button_experiment_next_step) {
				if(!canGoToNextPage()) {
					Toast.makeText(mContext, "請重新選擇可用的藍芽", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(mCurrentSelectedBTName != null && mCurrentSelectedBTAddress != null) { 
					//save current selected BT and use this setting
					Editor editor = mBTSettings.edit();
					editor.putString(ProjectConfig.Key_Preference_LastSelectedBT, mCurrentSelectedBTName);
					editor.putString(ProjectConfig.Key_Preference_CurrentSelectedBTAddress, mCurrentSelectedBTAddress);
					editor.commit();	
				}
				//else use old setting
				
				Intent intent = new Intent(mContext, ExperimentActivity.class);
				startActivity(intent);
				
				//(new LoadActivityAsyncTask(mContext, ExperimentActivity.class)).execute();
			}
			
		}
	};
	
	private BroadcastReceiver infoReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(action.equals(BluetoothClientConnector.Msg_update_info)) {
				Bundle extraData = intent.getBundleExtra(BluetoothClientConnector.MsgBundleKey);
				if(extraData.getString(BluetoothClientConnector.Key_Info_identifier).equals(BluetoothClientConnector.Info_testConnection)) {
					Toast.makeText(mContext, extraData.getString(BluetoothClientConnector.Key_Info_content), Toast.LENGTH_SHORT).show();
				}
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
			if(viewBeUnclickable != null) {
				viewBeUnclickable.setClickable(true);
				viewBeUnclickable = null;
			}
				
			showProgressBar(false);
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
					int currentSelectedIndex = mDisplayAdapter.getSelectedIndex();
					if(currentSelectedIndex != -1) {
						String data = mDisplayAdapter.getItem(currentSelectedIndex);
						String[] splitedData = data.split("\n");
						if(splitedData != null && splitedData.length >= 2) {
							setBTNameAndAddress(splitedData[0], splitedData[1]);
							BluetoothDevice device = mBTManager.getDevice(splitedData[1]);
							if(!mBTManager.hasBondedWith(device)) {
								try {
									mBTManager.createBond(device);
								}
								catch(Exception e) {
									
								}
							}
						}
						else {
							Log.d(debug_tag, "failed to parse data in AlertDialog for selecting bt device");
						}
					}
					dismiss();
				}
				else if(which == dialog.BUTTON_NEGATIVE){
					dismiss();
				}
			}
		};
		
	}
	
}
