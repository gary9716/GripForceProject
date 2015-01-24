package com.mhci.gripandtipforce;

import java.io.InputStream;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class BluetoothClientDataReceiver {
	
	public final static String debug_tag = BluetoothClientDataReceiver.class.getName();
	
	
	private final static int bufferSize = 300;
	private final static int dataBodySize = 19*6;
	private final static int headerByte1 = 0x0D;
	private final static int headerByte2 = 0x0A;
	
	private BluetoothSocket mSocket;
	private BluetoothDevice mDevice;
	private Handler mWorkHandler;
	private HandlerThread mWorkerThread;
	private byte[] dataBuffer;
	
	private void initThreadAndHandler() {
		mWorkerThread = new HandlerThread("BTClientWorkerThread");
		mWorkerThread.start();
		mWorkHandler = new Handler(mWorkerThread.getLooper());
	}
	
	private void parsingDataAndStored(byte[] buffer) {
		
	}
	
	public BluetoothClientDataReceiver(BluetoothDevice device) {
		if(device != null) {
			initThreadAndHandler();
			mDevice = device;
			dataBuffer = new byte[bufferSize];
			mWorkHandler.post(new Runnable() { 
				@Override
				public void run() { //to connect BT device and get a Socket.
					// TODO Auto-generated method stub
					mSocket = null;
					try {
						mSocket = mDevice.createRfcommSocketToServiceRecord(ProjectConfig.UUIDForBT);
					}
					catch(Exception e) {
						mSocket = null;
						Log.d(debug_tag, e.getLocalizedMessage());	
					}
					
					if(mSocket != null) {
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
										else {
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
							try {
								mSocket.close();
							}
							catch(Exception e2) {
								Log.d(debug_tag, e2.getLocalizedMessage());
							}
						}
					}
					return;
					
				}
			});
		}
		else {
			Log.d(debug_tag, "device is null, failed to connect");
		}
	}
	
}
