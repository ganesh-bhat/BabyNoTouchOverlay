package com.ganzystudio.toddlerlock.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ganzystudio.toddlerlock.receiver.PowerOffHandler;

/**
 * Created by ganbhat on 3/24/2018.
 */

public class OverlayService extends Service  {
    public static final String CLEANUP = "babylock.CLEANUP";
    String LOG_TAG = "OverlayService";

    WindowManager wm;
    PowerOffHandler mScreenStateReceiver = new PowerOffHandler();

    private static final int PRESS_INTERVAL = 700;
    boolean volumeUp = false;
    boolean volumeDown = false;
    long volumeUpTime = 0;
    long volumeDownTime = Long.MAX_VALUE;

    FrameLayout overlayView = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setBlockingMode(true);
        overlayView = getOverlayView();

        Log.i(LOG_TAG,"started service");

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD

                                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                |  WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP;
        params.x = 0;
        params.y = 0;

        overlayView.setAlpha(0.7f);
        overlayView.setBackgroundColor(0xEC407A);
        overlayView.requestFocus();
        overlayView.setFocusable(true);
        overlayView.setFocusableInTouchMode(true);
        overlayView.setTag("BabyLockOverlayView");

        wm.addView(overlayView, params);

        Toast.makeText(getApplicationContext(),"Starting baby protection overlay",Toast.LENGTH_SHORT).show();


        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);

        Log.i(LOG_TAG,"Created the view and added");
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG,"Destroyed service");
        cleanup();
    }


    public FrameLayout getOverlayView() {
        return new FrameLayout(getApplicationContext()) {

            @Override
            public void onWindowFocusChanged(boolean hasWindowFocus) {
                super.onWindowFocusChanged(hasWindowFocus);
                if(!hasWindowFocus) {
                    Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    sendBroadcast(closeDialog);

                    ActivityManager am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

                    if (cn != null && cn.getClassName().equals("com.android.systemui.recent.RecentsActivity")) {
                        toggleRecents();
                    }
                }
            }

            private void toggleRecents() {
                Intent closeRecents = new Intent("com.android.systemui.recent.action.TOGGLE_RECENTS");
                closeRecents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                ComponentName recents = new ComponentName("com.android.systemui", "com.android.systemui.recent.RecentsActivity");
                closeRecents.setComponent(recents);
                startActivity(closeRecents);
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                Log.v("customViewGroup", "**********Intercepted");
                return true;
            }

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                int keyCode = event.getKeyCode();
                Log.i(LOG_TAG,"On key:"+keyCode);
                if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    volumeDown = true;
                    volumeDownTime = event.getDownTime();
                    Log.i(LOG_TAG,"volumeDown button pressed");
                }

                if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    volumeUp = true;
                    volumeUpTime  = event.getDownTime();
                    Log.i(LOG_TAG,"volumeUp up pressed");
                }

                if(volumeUp & volumeDown) {
                    Log.i(LOG_TAG,"Both pressed");
                    Log.i(LOG_TAG,"volumeUp time:"+volumeUpTime);
                    Log.i(LOG_TAG,"volumeDown time:"+volumeDownTime);

                    if(volumeUpTime - volumeDownTime>=0 && volumeUpTime- volumeDownTime <= PRESS_INTERVAL) {
                        volumeUp = false;
                        volumeDown =false;
                        volumeUpTime = 0;
                        volumeDownTime = Long.MAX_VALUE;
                        Log.i(LOG_TAG,"Stopping service, as both volumeDown and volumeUp down pressed");

                        Toast.makeText(getApplicationContext(),"Stopping baby protection overlay",Toast.LENGTH_SHORT).show();

                        cleanup();

                        stopSelf();
                    }
                }
                return true;
            }
        };
    }

    private void cleanup() {
        if (overlayView != null) {
            wm.removeView(overlayView);
            overlayView = null;
        }
        sendBroadcast(new Intent(CLEANUP));

        try{
            unregisterReceiver(mScreenStateReceiver);
        } catch(Exception e) {
            //ignore
        }

       setBlockingMode(false);

    }

    public void setBlockingMode(boolean blockingMode) {
        if(KeyHandlerAccessibilityService.service!=null) {
            KeyHandlerAccessibilityService service = KeyHandlerAccessibilityService.service.get();
            service.setBlockingMode(blockingMode);
        }
    }
}
