package com.example.tatsuhiko.modeswitchingcamera.suppinCamera;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.util.Log;

/**
 * Created by tatsuhiko on 2017/06/18.
 */

public class CameraViewModel extends AndroidViewModel {

    private static final String TAG = "XXXXXX CameraViewModel";

    private int mCalledNum;
    private final Application mApplication;

    public CameraViewModel(Application application) {
        super(application);
        mApplication = application;
        mCalledNum = 0;
        Log.e(TAG, "Initialize mCalledNum to " + mCalledNum);
    }

    public void incrementCount() {
        Log.e(TAG, "Increment from " + mCalledNum + " to " + (mCalledNum + 1));
        mCalledNum ++;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.e(TAG, "mCalledNum is reset onCleared");
        mCalledNum = 0;
    }
}
