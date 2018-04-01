package com.ganzystudio.toddlerlock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ganzystudio.toddlerlock.service.OverlayService;
import com.ganzystudio.toddlerlock.ui.MainActivity;

/**
 * Created by ganbhat on 3/24/2018.
 */

public class NotificationActionListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("com.ganzystudio.babynotouchoverlay.LOCK")) {
            Intent intent2 = new Intent(context,OverlayService.class);
            context.startService(intent2);
        } else if(intent.getAction().equals("com.ganzystudio.babynotouchoverlay.CANCEL")) {
            MainActivity.removeNotification(context);
            //just to be safe
            Intent intent2 = new Intent(context,OverlayService.class);
            context.stopService(intent2);
        }
    }
}
