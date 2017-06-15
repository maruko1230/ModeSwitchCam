package com.example.tatsuhiko.modeswitchingcamera.suppinCamera;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.example.tatsuhiko.modeswitchingcamera.CameraActivity;
import com.example.tatsuhiko.modeswitchingcamera.CameraApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tatsuhiko on 2017/06/15.
 */

public class CameraDeviceHandler {

    private static final String TAG = "CameraDeviceHandler";

    private CameraDevice mCamera;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundThreadHandler;
    private PlatformCapability mPlatformCapability;

    private TextureView mViewFinderTextureView;

    private String mCameraId;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private CameraCaptureSession mCaptureSession;

    private int mDisplayOrientation;

    public CameraDeviceHandler(int orientation) {
        startBackgroundThread();
        setDisplayOrientation(orientation);
    }

    public void setDisplayOrientation(int orientation) {
        Log.e(TAG, "Orientation is changed to " + orientation);
        mDisplayOrientation = orientation;
    }

    public void startCamera(TextureView viewfinder) {
        Log.e(TAG, "startCamera");
        mViewFinderTextureView = viewfinder;
        if (viewfinder.isAvailable()) {
            Log.e(TAG, "TextureView is available");
            openCamera(viewfinder.getWidth(), viewfinder.getHeight());
        } else {
            Log.e(TAG, "TextureView is not available");
            viewfinder.setSurfaceTextureListener(mViewFinderTextureListener);
        }
    }

    public void releaseCamera() {
        Log.e(TAG, "releaseCamera");
        if (mCamera != null) {
            mCamera.close();
            mCamera = null;
        }
        stopBackgroundThread();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraDeviceHandler");
        mBackgroundThread.start();
        mBackgroundThreadHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundThreadHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openCamera(int width, int height) {
        Log.e(TAG, "openCamera");
        CameraManager cameraManager = (CameraManager)CameraApplication.getContext().
                getSystemService(Context.CAMERA_SERVICE);

        if (mPlatformCapability == null) {
            mPlatformCapability = new PlatformCapability();
        }
        mCameraId = mPlatformCapability.getCameraIdForMain();
        mPlatformCapability.getPreviewSize(mCameraId);

        try {
            cameraManager.openCamera(mCameraId, mCameraStateCallback, mBackgroundThreadHandler);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException");
        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException");
        }
    }

    private class PlatformCapability {

        private static final String TAG = "PlatformCapability";

        private final Map<String, CameraCharacteristics> capabilityMap = new HashMap<>();
        private final List<String> cameraIdList = new ArrayList<>();

        public PlatformCapability() {
            CameraManager cameraManager = (CameraManager)CameraApplication.getContext().
                    getSystemService(Context.CAMERA_SERVICE);

            try {
                for (String cameraId : cameraManager.getCameraIdList()) {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                    cameraIdList.add(cameraId);
                    capabilityMap.put(cameraId, characteristics);
                }

            } catch (CameraAccessException e) {

            }
        }

        public String getCameraIdForMain() {
            for (String id: cameraIdList) {
                CameraCharacteristics characteristics = capabilityMap.get(id);
                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (CameraCharacteristics.LENS_FACING_BACK == facing) {
                    Log.e(TAG, "main camera = " + id);
                    return id;
                }
            }
            Log.e(TAG, "Main camera is not found!");
            return null;
        }

        public Size getPreviewSize(String cameraId) {
            CameraCharacteristics characteristics = getCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            boolean swapDimension = false;
            switch (mDisplayOrientation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (sensorOrientation == 90  || sensorOrientation == 270) {
                        swapDimension = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (sensorOrientation == 0 || sensorOrientation == 180) {
                        swapDimension = true;
                    }
                    break;
            }

            for(Size size: map.getOutputSizes(ImageFormat.JPEG)) {
                Log.e(TAG, "Supporting JPG SIZE = " + size.getWidth() + "x" + size.getHeight());
                //TODO: Size is fixed to 640 due to sim. specification
                if (!swapDimension) {
                    if(size.getWidth() == 480 && size.getHeight() == 640) {
                        Log.e(TAG, "720p is supported");
                        return size;
                    }
                } else {
                    if(size.getWidth() == 640 && size.getHeight() == 480) {
                        Log.e(TAG, "720p is supported");
                        return size;
                    }
                }


            }
            Log.e(TAG, "720p is not supported");
            return null;
        }

        private CameraCharacteristics getCharacteristics(String id) {
            return capabilityMap.get(id);
        }
    }

    private final CameraDevice.StateCallback mCameraStateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice cameraDevice) {
                    mCamera = cameraDevice;
                    createPreviewSession();
                }

                @Override
                public void onDisconnected(CameraDevice cameraDevice) {
                    releaseCamera();

                }

                @Override
                public void onError(CameraDevice cameraDevice, int i) {
                    releaseCamera();
                }
            };


    private void createPreviewSession() {
        SurfaceTexture texture = mViewFinderTextureView.getSurfaceTexture();
        assert texture != null;

        Size previewSize = mPlatformCapability.getPreviewSize(mCameraId);
        texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

        Surface surface = new Surface(texture);

        try {
            mPreviewRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            mCamera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCamera) return;
                    mCaptureSession = cameraCaptureSession;

                    try {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);

                        mPreviewRequest = mPreviewRequestBuilder.build();
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundThreadHandler);
                    } catch (CameraAccessException e) {

                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    private final TextureView.SurfaceTextureListener mViewFinderTextureListener =
            new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    Log.e(TAG, "onSurfaceTextureAvailable: size: " + width + " x " + height);
                    openCamera(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                    Log.e(TAG, "onSurfaceTextureSizeChanged: size: " + width + " x " + height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

                }
            };

}
