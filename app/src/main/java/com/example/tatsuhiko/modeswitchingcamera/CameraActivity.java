package com.example.tatsuhiko.modeswitchingcamera;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraDevice;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.tatsuhiko.modeswitchingcamera.suppinCamera.CameraDeviceHandler;

public class CameraActivity extends Activity {

    private TextureView mCameraViewfinder;

    private CameraDeviceHandler mCameraDeviceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        configureWindow();
        mCameraViewfinder = (TextureView)findViewById(R.id.camera_viewfinder);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (Configuration.ORIENTATION_UNDEFINED == newConfig.orientation) {
            return;
        }
        super.onConfigurationChanged(newConfig);
        if(mCameraDeviceHandler != null) {
//            mCameraDeviceHandler.setDisplayOrientation(newConfig.orientation);
        }
    }


    /**
     * Sets Window flags, keep screen on, fullscreen, no rotation animation, application works
     * in LockScreen.
     */
    private void configureWindow() {
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final boolean seamless = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1);
        win.getAttributes().rotationAnimation = (seamless) ?
                3 /* ROTATION_ANIMATION_SEAMLESS*/ :
                WindowManager.LayoutParams.ROTATION_ANIMATION_JUMPCUT;
        win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        //Setting null drawable causes aliasing artifacts on button borders. Transparent instead
        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

}
