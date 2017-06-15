package com.example.tatsuhiko.modeswitchingcamera;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.view.TextureView;

import com.example.tatsuhiko.modeswitchingcamera.suppinCamera.CameraDeviceHandler;

public class CameraActivity extends Activity {

    private TextureView mCameraViewfinder;

    private CameraDeviceHandler mCameraDeviceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mCameraViewfinder = (TextureView)findViewById(R.id.camera_viewfinder);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(mCameraDeviceHandler != null) {
            mCameraDeviceHandler.setDisplayOrientation(newConfig.orientation);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraDeviceHandler = new CameraDeviceHandler(getWindowManager().getDefaultDisplay().getRotation());
        mCameraDeviceHandler.startCamera(mCameraViewfinder);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraDeviceHandler.releaseCamera();
        mCameraDeviceHandler = null;
    }

}
