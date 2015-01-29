package com.mhci.gripandtipforce;

import java.util.ArrayList;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Pair;

public class VariableWatcher<T> {
	private ArrayList<Pair<Runnable, Boolean>> taskList;
	private Handler mHandler;
	private HandlerThread mWorkerThread;
	private T genericObj = null;
	public VariableWatcher() {
		taskList = new ArrayList<Pair<Runnable,Boolean>>();
		mWorkerThread = new HandlerThread("VariableWatcher");
		mWorkerThread.start();
		mHandler = new Handler(mWorkerThread.getLooper());
		genericObj = null;
	}
	
	public T get() {
		return genericObj;
	}
	
	public void set(T obj) {
		genericObj = obj;
		executeTasksInList();
	}
	
	public void registerTask(Runnable task, boolean runInAnotherThread) {
		taskList.add(new Pair<Runnable, Boolean>(task, runInAnotherThread));
	}
	
	public void unregisterTask(Runnable task) {
		for(Pair<Runnable, Boolean> taskInfo : taskList) {
			if(taskInfo.first.equals(task)) {
				taskList.remove(taskInfo);
				break;
			}
		}
	}
	
	private void executeTasksInList() {
		for(Pair<Runnable, Boolean> taskInfo : taskList) {
			boolean runInAnotherThread = taskInfo.second;
			if(runInAnotherThread) {
				mHandler.post(taskInfo.first);
			}
			else {
				taskInfo.first.run();
			}
		}
	}
	
	
}
