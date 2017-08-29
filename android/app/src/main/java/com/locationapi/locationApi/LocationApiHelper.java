package com.locationapi.locationApi;

import android.content.Context;
import java.lang.Boolean;
import android.util.Log;

public class LocationApiHelper {

    private static LocationApiHelper mInstance;
    private static Boolean enabled;

    private LocationApiHelper() {
        enabled = true;
    }

    public static synchronized LocationApiHelper getInstance() {
        if (mInstance == null) {
            mInstance = new LocationApiHelper();
        }
        return mInstance;
    }

    public void enableLocation(Boolean status) {
        Log.e("LOCATIONAPI", "enableLocation: " + status);
        enabled = status;
    }

    public Boolean isEnabled() {
        Log.e("LOCATIONAPI", "isEnabled: " + enabled);
        return enabled;
    }
}