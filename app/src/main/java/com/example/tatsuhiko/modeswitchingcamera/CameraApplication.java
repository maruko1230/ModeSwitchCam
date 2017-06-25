package com.example.tatsuhiko.modeswitchingcamera;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by tatsuhiko on 2017/06/15.
 */

public class CameraApplication extends Application {

    private static final String TAG = "XXXXX CameraApp";

    private static Context mContext;

    public CameraApplication() {
        super();
        mContext = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.e(TAG, "onTerminate");
    }

    public static Context getContext() {
        return mContext;
    }
}
