package com.mhci.gripandtipforce;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class BluetoothClientConnector extends Service{
	
	public final static String debug_tag = BluetoothClientConnector.class.getName();
	private final static String action_tag = ".act";
	private final static String msg_tag = ".msg";
	
	public final static String DeviceAddrKey = "BTDeviceAddress";
	public final static String Action_test_connection = BluetoothClientConnector.class.getName() + action_tag + ".test_connection";
	public final static String Action_start_receiving_data = BluetoothClientConnector.class.getName() + action_tag + ".start_receiving_data";
	public final static String update_info = "update_info";
	public final static String Msg_update_info = BluetoothClientConnector.class.getName() + msg_tag + "." + update_info; 
	
	public final static String MsgBundleKey = "extraDataBundle";
	public final static String Key_Info_identifier = update_info + ".identifier";
	public final static String Key_Info_content = update_info + ".content";
	public final static String Info_dataReceivingConnection = "dataReceivingConnection";
	public final static String Info_testConnection = "testConnection";
	
	private final static long durationForWaitingConnectionToBeSetUp = 10000; //in milli secs
	private final static int bufferSize = 300;
	private final static int dataBodySize = ProjectConfig.numBytesPerSensorStrip*6;
	//private final static int numBytesPerSensorStrip = ProjectConfig.numBytesPerSensorStrip;
	private final static int headerByte1 = 0x0D;
	private final static int headerByte2 = 0x0A;
	
	//private BluetoothAdapter mBTAdapter = null;
	private BluetoothManager mBTManager = null;
	private BluetoothSocket mSocket = null;
	private BluetoothDevice mDevice = null;
	private Handler mWorkHandler = null;
	private HandlerThread mWorkerThread = null;
	private byte[] dataBuffer;
	private boolean toTerminateConnection = false;
	private LocalBroadcastManager mLBCManager = null;
	/*
	public void startDataReceiving() {
		if(mDevice != null) {
			mWorkHandler.post(dataReceivingTask);
		}
		else {
			Log.d(debug_tag, "device is null, failed to do this task");
		}
	}
	
	public void testConnection() {
		if(mDevice != null) {
			mWorkHandler.post(testConnectionTask);
		}
		else {
			Log.d(debug_tag, "device is null, failed to do this task");
		}
	}
	
	public void toTerminateThreadAndConnection() {
		toTerminateConnection = true;
	}
	
	public BluetoothClientConnector(BluetoothDevice device, Context context) {
		if(device != null) {
			initThreadAndHandler();
			mDevice = device;
			dataBuffer = new byte[bufferSize];
		}
		else {
			Log.d(debug_tag, "device is null, maybe failed to connect later");
		}
	}
	*/
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		initThreadAndHandler();
		dataBuffer = new byte[bufferSize];
		toTerminateConnection = false;
		mLBCManager = LocalBroadcastManager.getInstance(this);
		//mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		mBTManager = new BluetoothManager(this, null, null);
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
	};
	
	private final IBinder theOnlyBinder = new LocalBinder();
	
	public class LocalBinder extends Binder {
		BluetoothClientConnector getService() {
			return BluetoothClientConnector.this; //I think it means to return first initiated instance
		}
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
			
			mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(ProjectConfig.UUIDForBT);
			//schedule a timer task because connect is a blocking IO operation. Use close to abort this function.
			if(testConnection) {
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
		
		private void parsingDataAndStored(byte[] buffer) {
			
		}
		
		private void startReceivingData() {
			try {
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
				Bundle extraData = new Bundle();
				extraData.putString(Key_Info_identifier, Info_dataReceivingConnection);
				extraData.putString(Key_Info_content, "連線斷開");
				broadcastMsg(Msg_update_info, extraData);
				tryToCloseSocket();
			}
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!toTerminateConnection) {
				if(mSocket == null) {
					getSocketAndConnect(false);
				}
				else {
					//inform UI thread that connection has been set up
					Bundle extraData = new Bundle();
					extraData.putString(Key_Info_identifier, Info_dataReceivingConnection);
					extraData.putString(Key_Info_content, "連線中");
					broadcastMsg(Msg_update_info, extraData);
					startReceivingData();
				}
			}
			
		}
	};
	
};
