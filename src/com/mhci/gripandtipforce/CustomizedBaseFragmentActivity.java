package com.mhci.gripandtipforce;

import com.jakewharton.viewpagerui.UnderlinesStyledFragmentActivity;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class CustomizedBaseFragmentActivity extends FragmentActivity{
	
	private boolean isSystemBarShown = true;
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "no back pressed allowed", Toast.LENGTH_SHORT).show();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.experiment_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case R.id.menuitem_end_application:
				endTheAppAfterCertainDuration(0);
				break;
			case R.id.menuitem_show_bottom_bar:
				if(isSystemBarShown) {
					hideSystemBar();
				}
				else {
					showSystemBar();
				}
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	protected void endTheAppAfterCertainDuration(long delayMillis) {
		(new Handler(getMainLooper())).postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(), UnderlinesStyledFragmentActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("EXIT", true);
				startActivity(intent);
			}
		}, delayMillis);
	}

	protected void showSystemBar() {
		if(!ProjectConfig.useSystemBarHideAndShow) {
			return;
		}
		String commandStr = "am startservice -n com.android.systemui/.SystemUIService";
		runAsRoot(commandStr);
		isSystemBarShown = true;
	}

	protected void hideSystemBar() {
		if(!ProjectConfig.useSystemBarHideAndShow) {
			return;
		}
		try {
			//REQUIRES ROOT
			Build.VERSION_CODES vc = new Build.VERSION_CODES();
			Build.VERSION vr = new Build.VERSION();
			String ProcID = "79"; //HONEYCOMB AND OLDER

			//v.RELEASE  //4.0.3
			if (vr.SDK_INT >= vc.ICE_CREAM_SANDWICH) {
				ProcID = "42"; //ICS AND NEWER
			}

			String commandStr = "service call activity " + 
				ProcID + " s16 com.android.systemui";
			runAsRoot(commandStr);
		} catch (Exception e) {
			// something went wrong, deal with it here
		}
		isSystemBarShown = false;
	}


	private void runAsRoot(String commandStr) {
		try {
			Command command = new Command(0, commandStr);
			RootTools.getShell(true).add(command);
		} catch (Exception e) {
			// something went wrong, deal with it here
		}
	}
}
