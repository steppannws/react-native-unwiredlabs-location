package com.locationapi.locationApi;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.widget.Toast;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

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
import java.lang.Throwable;

import com.unwiredlabs.locationapi.Location.LocationAdapter;
import com.unwiredlabs.locationapi.Location.UnwiredLocationListener;

public class LocationApiModule extends ReactContextBaseJavaModule {
	private String api_key = "API_KEY";
	private static final String DURATION_SHORT_KEY = "SHORT";
  	private static final String DURATION_LONG_KEY = "LONG";
  	private LocationAdapter locationAdapter = null;//new LocationAdapter(getApplicationContext(), "a9860eaa0fa9e1");
  	private Callback callback = null;
  	private ReactApplicationContext theContext;

    public LocationApiModule(ReactApplicationContext reactContext) {
        super(reactContext);
        theContext = reactContext;
    }

    @Override
	public String getName() {
	    return "LocationApi";
	}

	@Override
	public Map<String, Object> getConstants() {
		final Map<String, Object> constants = new HashMap<>();
		constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
		constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
		return constants;
	}

	@ReactMethod
	public void init() {
		Activity currentActivity = getCurrentActivity();
		try {
			locationAdapter = new LocationAdapter(theContext, api_key);
        	locationAdapter.setPriority(LocationAdapter.PRIORITY_BALANCED_POWER_ACCURACY);

        	Toast.makeText(currentActivity, "Location API started", Toast.LENGTH_SHORT).show();

        } catch(Throwable e) {
        	Toast.makeText(currentActivity, "Error starting LocationAdapter", Toast.LENGTH_LONG).show();
        	e.printStackTrace();
        }
	}

	@ReactMethod
	public void enableLocation(Boolean status) {
		LocationApiHelper.getInstance().enableLocation(status);
	}

	@ReactMethod
	public void getLocation(Callback locationCallback) {
		callback = locationCallback;
		Activity currentActivity = getCurrentActivity();

		Toast.makeText(currentActivity, "Getting position", Toast.LENGTH_SHORT).show();

		try {
			if(locationAdapter == null) {
				locationAdapter = new LocationAdapter(theContext, api_key);
		    	locationAdapter.setPriority(LocationAdapter.PRIORITY_BALANCED_POWER_ACCURACY);
			}

			UnwiredLocationListener unwiredLocationListener = new UnwiredLocationListener() {
	            @Override
	            public void onLocationChanged(Location location) {
	                //Call the function that will handle the location once returned
	                if (location != null) {
	                	String msg = "Lat: "+location.getLatitude() + " | Lng: "+location.getLongitude();
	                    Toast.makeText(getCurrentActivity(), msg, Toast.LENGTH_SHORT).show();
	                    callback.invoke(location.getLatitude(), location.getLongitude());
	                }
	            }
	        };
	        
	        locationAdapter.getLocation(unwiredLocationListener);
	    } catch (Throwable e) {
	    	Toast.makeText(currentActivity, "Error getting position", Toast.LENGTH_LONG).show();
	    	e.printStackTrace();
	    }
	}
}