package com.example.dongja94.samplecamera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    Camera mCamera;
    int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private static final int RC_PERMISSION = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
            startPreview();
        }
    }

    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("permission");
                builder.setMessage("camera permission....");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, RC_PERMISSION);
                    }
                });
                builder.setCancelable(false);
                builder.create().show();
                return;
            }
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, RC_PERMISSION);
            return;
        }
        if (mCamera != null) {
            releaseCamera();
        }
        int numCamera = Camera.getNumberOfCameras();
        if (numCamera == 1) {
            mCamera = Camera.open();
        } else {
            mCamera = Camera.open(cameraId);
        }
    }

    private void releaseCamera() {
        mCamera.release();
        mCamera = null;
    }

    SurfaceHolder mHolder;

    private void startPreview() {
        if (mCamera != null && mHolder != null) {
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
        mHolder = holder;
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        stopPreview();

        mHolder = holder;
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
        mHolder = null;
        releaseCamera();
    }
}
