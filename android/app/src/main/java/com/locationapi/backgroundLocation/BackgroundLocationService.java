package com.locationapi.backgroundLocation;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.sql.Timestamp;
import java.util.Date;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.StringBuilder;
import java.lang.Throwable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.*;
import cz.msebera.android.httpclient.*;
import com.loopj.android.http.*;

import com.unwiredlabs.locationapi.Location.LocationAdapter;
import com.unwiredlabs.locationapi.Location.UnwiredLocationListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.locationapi.locationApi.LocationApiHelper;


public class BackgroundLocationService extends Service
{
    // TODO: provide api key from JS on initialization
    private String api_key = "API_KEY"; 
    private static final String TAG = "LOCATIONAPI";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 5000; // in milliseconds
    private static final float LOCATION_DISTANCE = 10f;
    private static String SERVER_URL = "";
    private static RequestQueue mQueue;
    private final ScheduledExecutorService schduler = Executors.newScheduledThreadPool(1);

    /**
     * Android location listener
     */
    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);

            if(SERVER_URL != "")
                sendLocationToServer(location);
            else {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String url = sharedPref.getString("server", "");

                if(url != "") {
                    SERVER_URL = url;
                    sendLocationToServer(location);
                }
            }

            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    /**
     * Boot service
     *
     * @param {Intent} intent
     * @param {int} flags
     * @param {int} startId
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand:: INIT");
        try {
            // Get saved server URL passed from JS
            InputStream inputStream = getApplicationContext().openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();

                // Set server URL
                SERVER_URL = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        Log.e(TAG, "onStartCommand:: URL " + SERVER_URL);

        // Send coordinates to server each 30 seconds
        new Thread(new Runnable() {
            public void run() {
                while(true) {
                   try {
                    Thread.sleep(30000);
                    startLocationService();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } 
                }
            }
        }).start();

        if(intent != null) {
            Log.e(TAG, "onStartCommand:: STARTUNG SUPER");
            super.onStartCommand(intent, flags, startId);
        } else {
            Log.e(TAG, "onStartCommand:: NULL INTENT");
        }

        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.e(TAG, "onCreate");

        // TODO: make switch between UnwiredLocation and Native location services

        // Initialize UnwiredLocationListener
        initializeLocationManager();

        // Request location to Android location listener
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    /**
     * Destroy Location Manager
     *
     */
    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();

        // schduler.shutdown();

        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    /**
     * Initialize LocationManager
     *
     */
    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    /**
     * Start Unwired Location service
     *
     */
    private void startLocationService() {
        Log.e(TAG, "startLocationService");

        try {
            final LocationAdapter locationAdapter = new LocationAdapter(getApplicationContext(), api_key);

            locationAdapter.setPriority(LocationAdapter.PRIORITY_BALANCED_POWER_ACCURACY);

            UnwiredLocationListener unwiredLocationListener = new UnwiredLocationListener() {
                /**
                 * Fire new location to server when it changes
                 */
                @Override
                public void onLocationChanged(Location location) {
                    //Call the function that will handle the location once returned
                    if (location != null)
                        sendLocationToServer(location);
                }
            };
            
            // Call to get location promise
            locationAdapter.getLocation(unwiredLocationListener);
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Send JSON object with coordinates and timestamp to server
     *
     * @param {Location} location
     */
    private void sendLocationToServer(Location location) {
        Log.e(TAG, "sendLocationToServer:: URL: " + SERVER_URL);

        if(SERVER_URL != "") {
            mQueue = CustomVolleyRequestQueue.getInstance(getApplicationContext()).getRequestQueue();
            String timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            
            JSONObject loc = new JSONObject();
            try {
                loc.put("lat", new Double(location.getLatitude()));
                loc.put("lng", new Double(location.getLongitude()));
            } catch(JSONException e) {
                e.printStackTrace();
            }

            JSONObject params = new JSONObject();
            try {
                params.put("location", loc);
                params.put("timestamp", timeStamp);
            } catch(JSONException e) {
                e.printStackTrace();
            }

            CustomJSONObjectRequest postRequest = new CustomJSONObjectRequest(Request.Method.POST, SERVER_URL, params,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, "######### SUCCESS #########" + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
            );
            mQueue.add(postRequest);
        }
    }
}