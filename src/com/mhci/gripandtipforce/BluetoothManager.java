package com.mhci.gripandtipforce;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class BluetoothManager extends BroadcastReceiver{
	public final static String DEBUG_TAG = "MyBTManager";
	
	private BluetoothAdapter mBTAdapter = null;
	private Context mContext = null;
	private LinkedList<BroadcastReceiver> registeredReceivers = null;
	private ArrayAdapter<String> mDataAdapter = null;
	private boolean mOriginalBTStateEnabled = false;
	
	
	/*
	private static BluetoothManager instance = null;
	public static BluetoothManager getInstance(Context context, BroadcastReceiver customizedReceiver, ArrayAdapter<String> dataAdapter) {
		if(instance == null) {
			synchronized (BluetoothManager.class) {
				if(instance == null) {
					instance = new BluetoothManager(context, customizedReceiver, dataAdapter);
				}
			}
		}
		
		if(context != null) {
			instance.mContext = context;
		}
		if(customizedReceiver != null) {
			instance.setBroadcastReceiver(customizedReceiver);
		}
		if(dataAdapter != null) {
			instance.mDataAdapter = dataAdapter;
		}
		
		return instance;
	}
	*/
	
	public BluetoothManager(Context context, BroadcastReceiver customizedReceiver, ArrayAdapter<String> dataAdapter) {
		mContext = context;
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBTAdapter == null) {
			Toast.makeText(mContext, "your device doesn't support bluetooth. Bluetooth Feature turned off", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(!mBTAdapter.isEnabled()) {
			mOriginalBTStateEnabled = false;
			enableBT(true);
		}
		else {
			mOriginalBTStateEnabled = true;
		}
		
		registeredReceivers = new LinkedList<BroadcastReceiver>();
		setBroadcastReceiver(customizedReceiver);
		
		mDataAdapter = dataAdapter;
		
		if(dataAdapter == null) {
			Log.d(DEBUG_TAG, "dataAdapter is null");
		}
		
		setOfDevices = new HashSet<String>();
		
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		if(registeredReceivers != null) {
			for(BroadcastReceiver receiver : registeredReceivers) {
				mContext.unregisterReceiver(receiver);
			}
		}
		
		if(mOriginalBTStateEnabled) {
			mBTAdapter.enable();	
		}
		else {
			mBTAdapter.disable();
		}
		mBTAdapter = null;
		mDataAdapter = null;
		mContext = null;
		
	}
	
	public void setBroadcastReceiver(BroadcastReceiver customizedReceiver) {
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		
		// Don't forget to unregister during onDestroy	
		mContext.registerReceiver(this, filter);
		registeredReceivers.add(this);
		
		if(customizedReceiver != null) {
			mContext.registerReceiver(customizedReceiver, filter); 
			registeredReceivers.add(customizedReceiver);
		}
		
	}
	
	private Set<String> setOfDevices; 
	
	//BT event listener
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceAddr = device.getAddress();
            if(mDataAdapter != null && !setOfDevices.contains(deviceAddr)) {
            		// Add the name and address to an array adapter to show in a ListView
            		mDataAdapter.add(device.getName() + "\n" + deviceAddr);
            		setOfDevices.add(deviceAddr);
            }
            else {
            		Log.d(DEBUG_TAG, "mDataAdapter is null, unable to add data");
            }
        }

	}
	
	public void enableBT(boolean enable) {
		if(mBTAdapter != null) {
			if(enable) {
				mBTAdapter.enable();
			}
			else {
				mBTAdapter.disable();
			}
		}
		else {
			Toast.makeText(mContext, "this device couldn't enable or disable Bluetooth", Toast.LENGTH_LONG).show();
		}
	}
	
	public void startDiscovering() {
		if(mBTAdapter == null || mDataAdapter == null) {
			return;
		}
		setOfDevices.clear();
		mDataAdapter.clear();
		if(!mBTAdapter.startDiscovery()) {
			Toast.makeText(mContext, "failed to start discovering bluetooth devices", Toast.LENGTH_LONG).show();
		}
	}
	
	public void stopDiscovering() {
		if(mBTAdapter.isDiscovering()) {
			mBTAdapter.cancelDiscovery();
		}	
	}
	
	public void enableDiscoverability(int durationInSecs) {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, durationInSecs);
		mContext.startActivity(discoverableIntent);
	}
	
	public void getBondedDevicesInfoAndUpdate() {
		if(mBTAdapter == null || mDataAdapter == null) {
			return;
		}
		
		mDataAdapter.clear(); //first clear previous data   
		
		Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
		// Loop through paired devices
	    for (BluetoothDevice device : pairedDevices) {
	        // Add the name and address to an array adapter to show in a ListView
	        mDataAdapter.add(device.getName() + "\n" + device.getAddress());
	    }
		
		return;
	}
	
	public boolean createBond(BluetoothDevice btDevice) throws Exception { 
        //pretty dirty code that use reflection
		Method createBondMethod = btDevice.getClass().getMethod("createBond");  
		Boolean returnValue = false;
		if(createBondMethod != null) {
			returnValue = (Boolean) createBondMethod.invoke(btDevice);  
        }
		else {
			Log.d(DEBUG_TAG, "reflection here may not work anymore");
		}
		return returnValue.booleanValue();  
    }  
	
	public BluetoothDevice getDevice(String addr) {
		return mBTAdapter.getRemoteDevice(addr);
	}
	
	public boolean hasBondedWith(BluetoothDevice device) {
		return mBTAdapter.getBondedDevices().contains(device);
	}


}
