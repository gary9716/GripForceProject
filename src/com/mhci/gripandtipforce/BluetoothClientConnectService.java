package com.mhci.gripandtipforce;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.lang.StringBuffer;

import javax.security.auth.PrivateCredentialPermission;

public class BluetoothClientConnectService extends Service{
	
	public final static String debug_tag = BluetoothClientConnectService.class.getName();
	private final static String action_tag = ".act";
	private final static String msg_tag = ".msg";
	
	public final static String DeviceAddrKey = "BTDeviceAddress";
	public final static String Action_test_connection = BluetoothClientConnectService.class.getName() + action_tag + ".test_connection";
	public final static String Action_start_receiving_data = BluetoothClientConnectService.class.getName() + action_tag + ".start_receiving_data";
	public final static String update_info = "update_info";
	public final static String Msg_update_info = BluetoothClientConnectService.class.getName() + msg_tag + "." + update_info; 
	
	public final static String MsgBundleKey = "extraDataBundle";
	public final static String Key_Info_identifier = update_info + ".identifier";
	public final static String Key_Info_content = update_info + ".content";
	public final static String Info_dataReceivingConnection = "dataReceivingConnection";
	public final static String Info_testConnection = "testConnection";
	
	private final static long durationForWaitingConnectionToBeSetUp = 10000; //in milli secs
	private final static int bufferSize = 300;
	private final static int totalNumDataPoints = ProjectConfig.numBytesPerSensorStrip * ProjectConfig.numSensorStrips;
	private final static int dataBodySize = bufferSize > totalNumDataPoints ? totalNumDataPoints : bufferSize;
	//private final static int numBytesPerSensorStrip = ProjectConfig.numBytesPerSensorStrip;
	private final static int headerByte1 = 0x0D;
	private final static int headerByte2 = 0x0A;
	
	private boolean toStoreData = false;
	
	private BluetoothManager mBTManager = null;
	private BluetoothSocket mSocket = null;
	private BluetoothDevice mDevice = null;
	private Handler mWorkHandler = null;
	private HandlerThread mWorkerThread = null;
	private byte[] dataBuffer;
	private boolean toTerminateConnection = false;
	private LocalBroadcastManager mLBCManager = null;
	private TxtFileManager txtFileManager = null;
	private String mUserID = ProjectConfig.defaultUserID;
	private final int logFileIndex = 0;
	private long startingTimestamp = 0;
	
	
	//API for Bounded Service
	public void setStoringDataEnabled(boolean enable) {
		toStoreData = enable;
	}
	
	public void setStartingTimestamp(long timestamp) {
		startingTimestamp = timestamp;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		initThreadAndHandler();
		dataBuffer = new byte[bufferSize];
		toTerminateConnection = false;
		mLBCManager = LocalBroadcastManager.getInstance(this);
		mBTManager = new BluetoothManager(this, null, null);
		SharedPreferences preferences = this.getSharedPreferences(ProjectConfig.Key_Preference_UserInfo, Context.MODE_PRIVATE);
		if(preferences != null) {
			mUserID = preferences.getString(ProjectConfig.Key_Preference_UserID, ProjectConfig.defaultUserID);
		}
		
		FileDirInfo dirInfo = new FileDirInfo(FileType.Log, ProjectConfig.getDirpathByID(mUserID), null);
		txtFileManager = new TxtFileManager(dirInfo, this);
	}
	
	private BluetoothDevice getBTDevice(Intent intent) {
		String deviceAddr = intent.getStringExtra(DeviceAddrKey);
		if(deviceAddr != null) {
			return mBTManager.getDevice(deviceAddr);
		}
		else {
			return null;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if(action.equals(Action_test_connection)) {
			mDevice = getBTDevice(intent);
			if(mDevice != null) {
				mWorkHandler.post(testConnectionTask);
			}
			else {
				Log.d(debug_tag, "illegal bt device");
			}
			
			return Service.START_NOT_STICKY;
		}
		else{
			Log.d(debug_tag,"illegal action to start this service, so do nothing but stop self");
			stopSelf();
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if(action.equals(Action_start_receiving_data)) {
			mDevice = getBTDevice(intent);
			toStoreData = false;
			if(mDevice != null) {
				mWorkHandler.post(dataReceivingTask);
			}
			else {
				Log.d(debug_tag, "illegal bt device");
			}
		}
		else{
			Log.d(debug_tag,"illegal action to bind this service, so do nothing but stop self");
			stopSelf();
			return null;
		}
		
		return theOnlyBinder;
	}
	
	public void onDestroy() {
		Log.d(debug_tag, "onDestroy in BluetoothClientConnector");
		toTerminateConnection = true;
		tryToCloseSocket();
		
		if(mWorkHandler != null) {
			mWorkHandler = null;
		}
		
		if(mWorkerThread != null) {
			mWorkerThread.quit();
			mWorkerThread = null;
		}
		txtFileManager.closeFile(logFileIndex);
	};
	
	private final IBinder theOnlyBinder = new LocalBinder();
	
	public class LocalBinder extends Binder {
		BluetoothClientConnectService getService() {
			return BluetoothClientConnectService.this; //I think it means to return first initiated instance
		}
	}
	
	private void createOrOpenBTDataLog() {
		mWorkHandler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				Intent action = new Intent(TaskRunnerAndDisplayProgressDialogAsyncTask.startAsyncTask);
				action.putExtra(TaskRunnerAndDisplayProgressDialogAsyncTask.Key_title, "開啟Log檔案");
				action.putExtra(TaskRunnerAndDisplayProgressDialogAsyncTask.Key_msg, "正在開啟藍芽Log檔案,請稍候");
				mLBCManager.sendBroadcast(action);
				
				txtFileManager.createOrOpenLogFileSync(ProjectConfig.getGripForceLogFileName(mUserID), logFileIndex);
				
				action.setAction(TaskRunnerAndDisplayProgressDialogAsyncTask.stopAsyncTask);
				mLBCManager.sendBroadcast(action);
			}
		});
	}
	
	private void initThreadAndHandler() {
		mWorkerThread = new HandlerThread("BTClientWorkerThread");
		mWorkerThread.start();
		mWorkHandler = new Handler(mWorkerThread.getLooper());
	}
	
	private void tryToCloseSocket() {
		if(mSocket == null) {
			return;
		}
		try {
			mSocket.close();
		}
		catch(Exception e) {
			
		}
		mSocket = null;
		return;
	}
	
	private void broadcastMsg(String msg, Bundle extraData) {
		Intent msgIntent = new Intent(msg);
		msgIntent.putExtra(MsgBundleKey, extraData);
		mLBCManager.sendBroadcast(msgIntent);
	}
	
	private Timer mTimer = new Timer();
	
	private TimerTask timerTaskForClosingSocket = new TimerTask() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			tryToCloseSocket();
		}
	};
	
	private void startTimerForClosingSocket(long delayInMilliSecs){
		try{
			mTimer.cancel();
			mTimer.schedule(timerTaskForClosingSocket, delayInMilliSecs);
		}
		catch(Exception e) {
			
		}
		
	}
	
	private boolean isConnectionSucceeded = false;
	private boolean isPairing = false;
	private void getSocketAndConnect(boolean testConnection) {
		mSocket = null;
		try {
			if(!mBTManager.hasBondedWith(mDevice)) {
				mBTManager.createBond(mDevice);
				isPairing = true;
				return;
			}
			
			mBTManager.stopDiscovering();
			
			mSocket = mDevice.createRfcommSocketToServiceRecord(ProjectConfig.UUIDForBT);
			//mSocket = mDevice.createRfcommSocketToServiceRecord(ProjectConfig.UUIDForBT);
			if(testConnection) {
				//schedule a timer task because connect is a blocking IO operation. Use close to abort this function.
				startTimerForClosingSocket(durationForWaitingConnectionToBeSetUp);
			}
			
			mSocket.connect();
			isConnectionSucceeded = true;
			
		}
		catch(Exception e) {
			mSocket = null;
			Log.d(debug_tag, e.getLocalizedMessage());	
			return;
		}
	}
	
	private Runnable testConnectionTask = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			isPairing = false;
			isConnectionSucceeded = false;
			getSocketAndConnect(true);
			Bundle extraData = new Bundle();
			if(isConnectionSucceeded) {
				//Connection succeeded and send message through broadcasting
				extraData.putString(Key_Info_identifier, Info_testConnection);
				extraData.putString(Key_Info_content, "連線成功");
				broadcastMsg(Msg_update_info, extraData);
			}
			else if(!isPairing){
				//Connection failed and send message through broadcasting
				extraData.putString(Key_Info_identifier, Info_testConnection);
				extraData.putString(Key_Info_content, "連線失敗");
				broadcastMsg(Msg_update_info, extraData);
			}
		}
	};
	
	private Runnable dataReceivingTask = new Runnable() { 
		private final static String debug_tag = "dataReceivingTask";
		private StringBuffer stringBuffer = new StringBuffer();
		private Bundle mExtraData = new Bundle();
		
		private void updateUIBTState(String msg) {
			mExtraData.putString(Key_Info_identifier, Info_dataReceivingConnection);
			mExtraData.putString(Key_Info_content, msg);
			broadcastMsg(Msg_update_info, mExtraData);
		}
		
		private void parsingDataAndStored(byte[] buffer) {
			stringBuffer.setLength(0);
			
			int numStrips = ProjectConfig.numSensorStrips;
			int numBytesInOneSensorStrip = ProjectConfig.numBytesPerSensorStrip;
			long timestampToLog = ProjectConfig.getTimestamp(startingTimestamp);
			
			for(int sensorStripIndex = 0;sensorStripIndex < numStrips;sensorStripIndex++) {
				stringBuffer.append(timestampToLog);
				for(int sensorDataIndex = 0;sensorDataIndex < numBytesInOneSensorStrip;sensorDataIndex++) {
					stringBuffer.append(',');
					stringBuffer.append((int)((buffer[sensorStripIndex*numBytesInOneSensorStrip +  sensorDataIndex]) & 0xFF));
				}
				stringBuffer.append('\n');
			}
			
			if(toStoreData) {
				txtFileManager.appendLogWithNewlineSync(logFileIndex, stringBuffer.toString());
			}
		}
		
		private void delay(long millisec) {
			try {
				Thread.sleep(millisec);
			}
			catch(Exception e) {
				
			}
		}
		
		private void startReceivingData() {
			try {
				txtFileManager.createOrOpenLogFileSync(ProjectConfig.getGripForceLogFileName(mUserID), logFileIndex);
				InputStream inStream = mSocket.getInputStream();
				while(true) {
					if(inStream.read() == headerByte1 && inStream.read() == headerByte2) { //the body of data is behind header
						int dataSizeToBeRead = dataBodySize;
						int numDataSizeHasbeenRead = 0;
						int numDataSizeRead = 0;
						while((numDataSizeRead = inStream.read(dataBuffer, numDataSizeHasbeenRead, dataSizeToBeRead)) > 0) {
							dataSizeToBeRead = dataSizeToBeRead - numDataSizeRead;
							if(dataSizeToBeRead == 0) { //Parsing data
								parsingDataAndStored(dataBuffer);
								break;
							}
							else { //continue to read
								numDataSizeHasbeenRead += numDataSizeRead;
							}
						}
						
					}
				}
				
			}
			catch(Exception e) {
				Log.d(debug_tag,e.getLocalizedMessage());
			}
			finally {
				//inform UI thread connection is shut down.
				updateUIBTState("連線斷開,即將重新連線");
				tryToCloseSocket();
			}
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mSocket = null;
			while(!toTerminateConnection) {
				while(!mBTManager.isBTEnabled()) {
					updateUIBTState("藍芽開啟中");
					mBTManager.enableBT(true);
					delay(2000); //delay 2 secs
				}
				
				if(mSocket == null) {
					updateUIBTState("正在嘗試連線");
					getSocketAndConnect(false);
					if(mSocket == null) {
						updateUIBTState("連線失敗，1秒後重新連線");
						delay(1000); //delay 1 sec
					}
				}
				else {
					//inform UI thread that connection has been set up
					updateUIBTState("已連線，資料傳輸中");
					startReceivingData();
				}
			}
		}
	};
	
};
