package com.example.dongja94.samplecamera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    Camera mCamera;
    Gallery gallery;
    ImageAdapter mAdapter;

    int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        gallery = (Gallery)findViewById(R.id.gallery);
        mAdapter = new ImageAdapter();
        gallery.setAdapter(mAdapter);

        Button btn = (Button)findViewById(R.id.btn_change);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCamera();
            }
        });

        btn = (Button)findViewById(R.id.btn_effect);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeColorEffect();
            }
        });

        btn = (Button)findViewById(R.id.btn_picture);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

        }
    };
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            int width = options.outWidth;
            int height = options.outHeight;
            int realWidth = getResources().getDimensionPixelOffset(R.dimen.photo_width);
            int realHeight = getResources().getDimensionPixelOffset(R.dimen.photo_height);
            int wscale = width / realWidth;
            int hscale = height / realHeight;
            int scale = (wscale < hscale)? wscale : hscale;
            if (scale == 0) {
                scale = 1;
            }

            options = new BitmapFactory.Options();
            options.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

            mAdapter.add(bitmap);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startPreview();
                }
            }, 500);
        }
    };

    Handler mHandler = new Handler();
    private void takePicture() {
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    private void changeColorEffect() {
        Camera.Parameters parameters = mCamera.getParameters();
        final List<String> effectList = parameters.getSupportedColorEffects();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("color effect");
        builder.setItems(effectList.toArray(new String[effectList.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String effect = effectList.get(which);
                Camera.Parameters params = mCamera.getParameters();
                params.setColorEffect(effect);
                mCamera.setParameters(params);
            }
        });
        builder.create().show();
    }
    private void changeCamera() {
        if (Camera.getNumberOfCameras() <= 1) return;
        stopPreview();
        releaseCamera();
        cameraId = (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
        openCamera();
        startPreview();
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

    int[] orientations = {90, 0, 270, 180};

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
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        mCamera.setDisplayOrientation(orientations[rotation]);
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
