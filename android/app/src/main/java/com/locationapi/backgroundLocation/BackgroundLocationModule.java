package com.locationapi.backgroundLocation;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.widget.Toast;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import android.content.Context;

import java.util.Map;
import java.util.HashMap;

import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.StringBuilder;


public class BackgroundLocationModule extends ReactContextBaseJavaModule {
	private static final String DURATION_SHORT_KEY = "SHORT";
  	private static final String DURATION_LONG_KEY = "LONG";

    public BackgroundLocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
	public String getName() {
	    return "BackgroundLocation";
	}

	@Override
	public Map<String, Object> getConstants() {
		final Map<String, Object> constants = new HashMap<>();
		constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
		constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
		return constants;
	}

	/**
	 * Initialize BackgroundLocation from JS. Storing server url passed as a parameter
	 *
	 * @param {String} serverUrl
	 */
	@ReactMethod
	public void init(String serverUrl) {
		Activity currentActivity = getCurrentActivity();

		// Save server URL tu use it later to send location
		try {
			Toast.makeText(currentActivity, "Background Location Service started", Toast.LENGTH_SHORT).show();

	        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getReactApplicationContext().openFileOutput("config.txt", Context.MODE_PRIVATE));
	        outputStreamWriter.write(serverUrl);
	        outputStreamWriter.close();
	    }
	    catch (IOException e) {
	    	Toast.makeText(currentActivity, "Error starting Background Location Service", Toast.LENGTH_SHORT).show();
	    }

		// Start background service
		if(currentActivity != null && isMyServiceRunning(BackgroundLocationService.class) == false) {
			Intent i = new Intent(currentActivity, BackgroundLocationService.class);
			currentActivity.startService(i);
		}
	}

	/**
	 * Check if a service is already running
	 *
	 * @param {Class<?>} serviceClass
	 */
	private boolean isMyServiceRunning(Class<?> serviceClass) {
	  ActivityManager manager = (ActivityManager) getReactApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
	  for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    if (serviceClass.getName().equals(service.service.getClassName())) {
	        return true;
	    }
	  }
	  return false;
	}
}