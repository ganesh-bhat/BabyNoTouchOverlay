package com.ganzystudio.toddlerlock.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import java.lang.ref.WeakReference;

import com.ganzystudio.toddlerlock.util.Constants;

/**
 * Created by ganbhat on 3/24/2018.
 */

public class KeyHandlerAccessibilityService extends AccessibilityService {
    private static final String TAG = "KeyHandlerAccService";

    public static WeakReference<KeyHandlerAccessibilityService> service;

    private WindowManager wm;
    private SharedPreferences sp;
    private boolean mBlockingMode;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Created accessibility service");
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        sp = getSharedPreferences(Constants.OVERLAY_SERVICE, MODE_APPEND);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "Accessibility evt Received:"+event.toString());

    }

    @Override
    public void onInterrupt() {
        // No interruptible actions taken by this service.
    }

    public void setBlockingMode(boolean flag) {
        mBlockingMode = flag;
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.i(TAG, "Received:"+event.toString());
        //consume all
        //return true;

        if(mBlockingMode) {
            Log.i(TAG, "Blocking mode on");
            if(event.getKeyCode() != KeyEvent.KEYCODE_VOLUME_DOWN &&
                    event.getKeyCode() != KeyEvent.KEYCODE_VOLUME_UP
                    ) {
                //consume it, else pass it on to view below

                Log.i(TAG, "Consuming event:"+event.toString());
                return true;
            }
        }
        return super.onKeyEvent(event);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        service = new WeakReference(this);

        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) {
            // If we fail to obtain the service info, the service is not really
            // connected and we should avoid setting anything up.
            return;
        }

        info.eventTypes = GLOBAL_ACTION_HOME |
                GLOBAL_ACTION_BACK |
                GLOBAL_ACTION_RECENTS |
                GLOBAL_ACTION_POWER_DIALOG |
                GLOBAL_ACTION_QUICK_SETTINGS;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;

        if (Build.VERSION.SDK_INT >= 16)
            info.flags |= AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(info);
        Log.i(TAG, "Accessibility info found and updated");

    }

    @Override
    public boolean onUnbind(Intent intent) {
        service = null;
        return super.onUnbind(intent);
    }
}
