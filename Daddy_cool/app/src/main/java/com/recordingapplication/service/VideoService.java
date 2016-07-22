package com.recordingapplication.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.recordingapplication.utill.ComonUtill;

import java.io.IOException;

/**
 * Service --  Video Recording
 *********************************************
 */
public class VideoService extends Service implements SurfaceHolder.Callback{

    private String TAG = "VideoRecorder:";

    private WindowManager windowManager ;
    private SurfaceView surfaceView = null;
    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;
    private int rotated_angle =270;
    private boolean prRecordInProcess;


    @Override
    public void onCreate() {
        setSurfaceWindow();
        prRecordInProcess = false;
    }

    private void setSurfaceWindow(){

        try{
            windowManager = (WindowManager) this
                    .getSystemService(Context.WINDOW_SERVICE);
            surfaceView = new SurfaceView(this);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                    1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            windowManager.addView(surfaceView, layoutParams);
            surfaceView.getHolder().addCallback(this);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        /*open front camera*/
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        if(cameraCount >1){
            for ( int camIdx = 0; camIdx < Camera.getNumberOfCameras(); camIdx++ ) {
                Camera.getCameraInfo( camIdx, cameraInfo );

                if(ComonUtill.SET_CAMERA == ComonUtill.CAMERA_TYPE_FRONT){
                    if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
                        try {
                            rotated_angle = 270;
                            camera = Camera.open( camIdx );
                        } catch (RuntimeException e) {
                            Log.i("Camera failed to open: ",e.getLocalizedMessage());
                        }
                    }
                }
                else if (ComonUtill.SET_CAMERA == ComonUtill.CAMERA_TYPE_BACK){
                    if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK  ) {
                        try {
                            rotated_angle = 90;
                            camera = Camera.open( camIdx );
                        } catch (RuntimeException e) {
                            Log.i("Camera failed to open: ",e.getLocalizedMessage());
                        }
                    }
                }

            }

        }else
            camera = Camera.open();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        camera.stopPreview();
        mediaRecorder = new MediaRecorder();
        camera.unlock();

        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);


        mediaRecorder.setOrientationHint(rotated_angle);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);


        //mediaRecorder.setMaxDuration(Global.maxDuration);

        //mediaRecorder.setProfile(CamcorderProfile
        //       .get(CamcorderProfile.QUALITY_HIGH));
        //ComonUtill.CreateFolder();
        mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory()
                + "/" + ComonUtill.DirName + "/" + "video_"
                + System.currentTimeMillis() + ".mp4");

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        prRecordInProcess = true;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "VideoService onDestroy");
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        camera.lock();
        camera.stopPreview();
        camera.release();
        windowManager.removeView(surfaceView);
        camera = null;
        this.stopSelf();
        ComonUtill.mIsVideoWorking = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null;}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            camera.setDisplayOrientation(rotated_angle);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        ComonUtill.mIsVideoWorking = false;
        if (prRecordInProcess) {
            this.stopSelf();
        }
    }


}