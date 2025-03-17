package com.example.myapplication;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.CamcorderProfile;
import android.media.EncoderProfiles;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    public final  int CAMERA_REQUEST_CODE_VIDEO = 200;
    Button btnStart, btnStop;
    SurfaceView video;
    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;
    int counter = 0;
    String defaultFilename ="amit.3gp";
    CameraDevice cameraDevice;
    Camera mCamera;
    int mDisplayRotation = 0,   mSensorOrientation = 0;
    Point mDisplaySize;
    Matrix mMatrix;
    private final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        video = findViewById(R.id.video);
        mDisplayRotation = this.getWindowManager().getDefaultDisplay().getRotation();
        Point mDisplaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(mDisplaySize);
        SurfaceView cameraView = (SurfaceView) findViewById(R.id.video);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void init(View view) {
        recorder = new MediaRecorder();
        initRecorder();

    }

    private void initRecorder() {
        if (checkPermissions()) {
            video.getHolder();

            String cameraId = "";
            try {
                CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
                String[] cams = manager.getCameraIdList();
                cameraId = manager.getCameraIdList()[1];
                manager.openCamera(cameraId, stateCallback, null);

            }   catch (CameraAccessException e) {

            } catch (SecurityException sx) {

            }

        } else
            requestPermissions();
    }


    public void init2() {

        prepareRecorder();
    }

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            try {
                cameraDevice = camera;
                CameraManager manager = (CameraManager) getBaseContext().getSystemService(Context.CAMERA_SERVICE);
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics("1");
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                int lensFacing = mSensorOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);


                mCamera = Camera.open(1);
                setCameraDisplayOrientation(1, mCamera);
                mCamera.unlock();

                recorder.setCamera(mCamera);
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                CamcorderProfile cpLow = CamcorderProfile
                        .get(Integer.parseInt("1"), CamcorderProfile.QUALITY_LOW);
                recorder.setProfile(cpLow);

                recorder.setOutputFile(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/" + defaultFilename);
                /*recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);*/
                counter += 1;
                recorder.setMaxDuration(50000); // 50 seconds
                recorder.setMaxFileSize(5000000); // Approximately 5 megabytes

                init2();
            }    catch (Exception e) {
                    String err = e.getMessage();
            }

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }

    };


    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
            Toast.makeText(getApplicationContext(), "Camera ready", Toast.LENGTH_LONG).show();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    public void setCameraDisplayOrientation(int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        try {
            mCamera.stopPreview();
            mCamera.setDisplayOrientation(result);
            mCamera.startPreview();
        } catch (Exception ex) {
            String err = ex.getMessage();
        }

    }

    public void onClick(View v) {
        if (recording) {
            recorder.stop();
            recording = false;

            String fname = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/" + defaultFilename;
            File file = new File(fname);
            int file_size = Integer.parseInt(String.valueOf(file.length()/1024));
            String msg =fname + " size: " + String.valueOf(file_size) + " kb";
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        } else {
            recorder.start();
            recording = true;
        }
    }

   @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

        holder.setFixedSize(width, height);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.release();
        mCamera = null;
    }

    public boolean checkPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
                && result2 == PackageManager.PERMISSION_GRANTED &&
                result3 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, CAMERA},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToRead = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToCamera = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (permissionToRecord && permissionToStore && permissionToRead && permissionToCamera) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                       initRecorder();

                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }


}