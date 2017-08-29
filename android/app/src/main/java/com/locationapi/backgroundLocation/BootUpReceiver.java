package com.locationapi.backgroundLocation;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if(!isMyServiceRunning(BackgroundLocationService.class, context)) {

		    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		    Intent service = new Intent(context, BackgroundLocationService.class);
		    PendingIntent pi = PendingIntent.getService(context, 0, service, PendingIntent.FLAG_UPDATE_CURRENT);

			// Fire alarm every "milliseconds"
			// am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 30000, pi);

			context.startService(service);

			// Restart alarm if device is rebooted
			ComponentName receiver = new ComponentName(context, BootUpReceiver.class);
			PackageManager pm = context.getPackageManager();
			pm.setComponentEnabledSetting(receiver,
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);

		}
	    Log.e("BOOT RECEIVER", "start service");
	}

	private boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.e("BOOT RECEIVER:: Service already","running");
                return true;
            }
        }
        Log.e("BOOT RECEIVER:: Service not","running");
        return false;
    }
}