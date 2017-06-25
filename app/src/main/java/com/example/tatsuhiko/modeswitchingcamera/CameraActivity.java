package com.example.tatsuhiko.modeswitchingcamera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.tatsuhiko.modeswitchingcamera.suppinCamera.CameraDeviceHandler;
import com.example.tatsuhiko.modeswitchingcamera.suppinCamera.CameraViewModel;


public class CameraActivity extends FragmentActivity {

    private static final String TAG = "XXXXXSS CameraActivity";

    private TextureView mCameraViewfinder;

    private CameraDeviceHandler mCameraDeviceHandler;

    private CameraViewModel mCameraViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        configureWindow();
        mCameraViewfinder = (TextureView)findViewById(R.id.camera_viewfinder);

        mCameraViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraViewModel.incrementCount();
        mCameraDeviceHandler = new CameraDeviceHandler(getWindowManager().getDefaultDisplay().getRotation());
        mCameraDeviceHandler.startCamera(mCameraViewfinder);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraDeviceHandler.releaseCamera();
        mCameraDeviceHandler = null;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.e(TAG, "Configuration changed");
        Display disp = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        disp.getRealSize(size);

        Log.e(TAG, "Display Orientation = " + disp.getRotation());
        Log.e(TAG, "Display Size = " + DimensToString(size.x, size.y));

        if (Configuration.ORIENTATION_UNDEFINED == newConfig.orientation) {
            return;
        }
        super.onConfigurationChanged(newConfig);

        if(mCameraDeviceHandler != null) {
            int rotation = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            mCameraDeviceHandler.setDisplayOrientation(rotation);
        }
    }

    public static String SizeToString(Size size) {
        return DimensToString(size.getWidth(), size.getHeight());
    }

    public static String DimensToString(int width, int height) {
        StringBuilder builder = new StringBuilder();
        builder.append(width);
        builder.append(" x ");
        builder.append(height);

        return builder.toString();

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
        win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //Setting null drawable causes aliasing artifacts on button borders. Transparent instead
        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

}
