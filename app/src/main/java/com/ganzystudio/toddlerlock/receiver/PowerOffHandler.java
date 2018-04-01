package com.ganzystudio.toddlerlock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.ganzystudio.toddlerlock.service.OverlayService;

/**
 * Created by ganbhat on 3/24/2018.
 */

public class PowerOffHandler extends BroadcastReceiver {

    PowerManager.WakeLock wakeLock;
    String LOG_TAG = "PowerOffHandler";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG,"Received event"+intent.getAction());
        if ((intent.getAction().equals(Intent.ACTION_SCREEN_OFF))) {
            Log.i(LOG_TAG,"Received screen off event");
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK| PowerManager.ON_AFTER_RELEASE, "TEST");
            if(wakeLock.isHeld()) {
                wakeLock.release();
            }
            wakeLock.acquire();

            wakeLock.release();

            /*AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent inten = new Intent(context,OverlayService.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, inten, 0);
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,  100, pi);*/

            Log.i(LOG_TAG,"Made screen on again");

        }

        if ((intent.getAction().equals(OverlayService.CLEANUP))) {
            finishWakeLocker();
        }
    }

    public void finishWakeLocker(){
        if (wakeLock != null)
            wakeLock.release();
    }
}
