package com.ganzystudio.toddlerlock.ui;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;

import java.util.Iterator;

import com.ganzystudio.toddlerlock.service.OverlayService;

public class MainActivity extends AppCompatActivity {

    public static final int NOTIFICATION_ID = 56789;
    public static final String APP_NAME = "BabyLock";
    public static final String IS_ALREADY_USED = "isAlreadyUsed";
    public static final String COM_GANZYSTUDIO_BABYNOTOUCHOVERLAY_LOCK = "com.ganzystudio.babynotouchoverlay.LOCK";
    public static final String COM_GANZYSTUDIO_BABYNOTOUCHOVERLAY_CANCEL = "com.ganzystudio.babynotouchoverlay.CANCEL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ganzystudio.com.toddlerlock.R.layout.activity_main);
        Button startBtn = (Button)findViewById(ganzystudio.com.toddlerlock.R.id.startBtn);
        Button endBtn = (Button)findViewById(ganzystudio.com.toddlerlock.R.id.endBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNotification();
            }
        });

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeNotification(getApplicationContext());
                //just to be safe
                Intent intent = new Intent(MainActivity.this,OverlayService.class);
                stopService(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences(APP_NAME,MODE_APPEND);
        boolean isOpenedAlready = sp.getBoolean(IS_ALREADY_USED,false);

        if(isOpenedAlready) {
            DialogInterface.OnClickListener agreeListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    SharedPreferences sp = getSharedPreferences(APP_NAME,MODE_APPEND);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean(IS_ALREADY_USED,true);
                    editor.commit();
                    checkPermission();
                }
            };
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(ganzystudio.com.toddlerlock.R.string.welcome_title)
                    .setMessage(ganzystudio.com.toddlerlock.R.string.welcome_mesage)
                    .setPositiveButton(getString(ganzystudio.com.toddlerlock.R.string.agree), agreeListener)
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            checkPermission();
        }
    }

    private void checkPermission() {
        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {

                DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 1234);
                    }
                };

                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(ganzystudio.com.toddlerlock.R.string.draw_permssion_titme)
                        .setMessage(ganzystudio.com.toddlerlock.R.string.draw_permission_message)
                        .setPositiveButton(ganzystudio.com.toddlerlock.R.string.daw_permission_agree, okListener)
                        .setNegativeButton(ganzystudio.com.toddlerlock.R.string.daw_permission_cancel,cancelListener)
                        .create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

            } else {
                checkAccessibility();
            }
        }
    }


    public void openAccessibility()
    {
        Intent localIntent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(localIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkAccessibility();
    }

    private void checkAccessibility() {
        if(!isAccessibilityEnabled()){
            DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openAccessibility();
                }
            };

            DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(ganzystudio.com.toddlerlock.R.string.accessibility_permission_title)
                    .setMessage(ganzystudio.com.toddlerlock.R.string.accessibility_permission_message)
                    .setPositiveButton(ganzystudio.com.toddlerlock.R.string.accessibility_permission_positive_btn, okListener)
                    .setNegativeButton(ganzystudio.com.toddlerlock.R.string.accessibility_permission_cancel_btn,cancelListener)
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

    }


    private void createNotification() {


        Intent lockIntent = new Intent(COM_GANZYSTUDIO_BABYNOTOUCHOVERLAY_LOCK);
        PendingIntent lockPendingIntent = PendingIntent.getBroadcast(this,0,lockIntent,0);

        NotificationCompat.Builder mBuilder = (android.support.v7.app.NotificationCompat.Builder)new NotificationCompat.Builder(this)
                .setContentTitle("Toddler Screen Lock")
                .setContentText("Lock your screen with single touch")
                .setSmallIcon(ganzystudio.com.toddlerlock.R.drawable.ic_action_lock)
                .setContentIntent(lockPendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent cancelIntent = new Intent(COM_GANZYSTUDIO_BABYNOTOUCHOVERLAY_CANCEL);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this,0,cancelIntent,0);
        mBuilder.addAction(ganzystudio.com.toddlerlock.R.drawable.ic_action_cancel,getString(ganzystudio.com.toddlerlock.R.string.notification_action_cancel),cancelPendingIntent);

        Intent configScreenIntent = new Intent(MainActivity.this,MainActivity.class);
        PendingIntent configScreenPendingIntent = PendingIntent.getActivity(this,0,configScreenIntent,0);
        mBuilder.addAction(ganzystudio.com.toddlerlock.R.drawable.ic_lock,getString(ganzystudio.com.toddlerlock.R.string.notification_action_config),configScreenPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static void removeNotification(Context context) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public boolean isAccessibilityEnabled()
    {
        AccessibilityManager accessibilityMgr = (AccessibilityManager)getApplicationContext().getSystemService(ACCESSIBILITY_SERVICE);
        if(accessibilityMgr.getEnabledAccessibilityServiceList(-1)!=null) {
            Iterator paramContext = accessibilityMgr.getEnabledAccessibilityServiceList(-1).iterator();
            while (paramContext.hasNext()) {
                String serviceId = ((AccessibilityServiceInfo) paramContext.next()).getId();
                Log.i("AccServices",serviceId);
                if ("ganzystudio.com.babynotouchoverlay/.KeyHandlerAccessibilityService".equals(serviceId)) {
                    return true;
                }
            }
        }

        return false;
    }
}
