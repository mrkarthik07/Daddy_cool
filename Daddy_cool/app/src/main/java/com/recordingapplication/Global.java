package com.recordingapplication;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Handler;

import com.recordingapplication.service.AppMonitorService;
import com.recordingapplication.service.LocationService;
import com.recordingapplication.service.AudioService;
import com.recordingapplication.service.VideoService;
import com.recordingapplication.utill.ComonUtill;

public class Global extends Application {

    public static Global gInstance;
    public static Activity gCurrentActivity;

    public static int maxDuration;

    public void onCreate() {
        super.onCreate();
        gInstance = this;
        ComonUtill.applicationContext = this;

        ComonUtill.CreateFolder();
    }

    /**
     *  App Monitor
     */
    public static void StartAppMonitor(){
        Intent intent = new Intent(ComonUtill.applicationContext, AppMonitorService.class);
        ComonUtill.applicationContext.startService(intent);
    }

    /**
     *  Location Pos
     */
    public static void StartLocation(){
        //Share init
        ComonUtill.saveDBfloat(ComonUtill.pre_latitude, 0.0f);
        ComonUtill.saveDBfloat(ComonUtill.pre_longitude, 0.0f);

        Intent wpsLocationService = new Intent(ComonUtill.applicationContext, LocationService.class);
        ComonUtill.applicationContext.stopService(wpsLocationService);
        ComonUtill.applicationContext.startService(wpsLocationService);
    }

    /**
     *  Video or Audio Recording
     */
    public static void StartRecording(int type){
        int secTime =  ComonUtill.mRecTime * 60 * 1000;
        ComonUtill.IntToDB(ComonUtill.PREFNAME, ComonUtill.max_recording_time, secTime);
        maxDuration = secTime;

        if(type == 0 && !ComonUtill.mIsVideoWorking){
            StartVideoRecordAround();
            StopVideoRecordAround();
            ComonUtill.mIsVideoWorking = true;
        }else if(type == 1 && !ComonUtill.mIsAudioWorking) {
            StartAudioRecordAround();
            StopAudioRecordAround();
            ComonUtill.mIsAudioWorking = true;
        }

    }

    public static void StartVideoRecordAround() {

        Intent intent = new Intent(ComonUtill.applicationContext,VideoService.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComonUtill.applicationContext.startService(intent);
    }

    private static void StopVideoRecordAround(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

                if(ComonUtill.mIsVideoWorking)
                    ComonUtill.applicationContext.stopService(new Intent(ComonUtill.applicationContext,VideoService.class));

                ComonUtill.mIsVideoWorking = false;
                if(!ComonUtill.bAllStop)
                    reRecVideo();
            }
        }, maxDuration);
    }

    public static void StartAudioRecordAround() {
        Intent intent = new Intent(ComonUtill.applicationContext,AudioService.class);
        intent.putExtra("commandType", 1);
        ComonUtill.applicationContext.startService(intent);
    }

    private static void StopAudioRecordAround(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

                if(ComonUtill.mIsAudioWorking){
                    Intent intent = new Intent(ComonUtill.applicationContext,AudioService.class);
                    intent.putExtra("commandType", 2);
                    ComonUtill.applicationContext.startService(intent);
                }
                ComonUtill.mIsVideoWorking = false;
                if(!ComonUtill.bAllStop)
                    reRecAudio();
            }
        }, maxDuration);
    }

    private static void reRecVideo(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

                if(!ComonUtill.mIsVideoWorking) {
                    StartVideoRecordAround();
                    StopVideoRecordAround();
                    ComonUtill.mIsVideoWorking = true;
                }
            }
        }, 20000);
    }

    private static void reRecAudio(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

                if(!ComonUtill.mIsAudioWorking){
                    StartAudioRecordAround();
                    StopAudioRecordAround();
                    ComonUtill.mIsAudioWorking = true;
                }
            }
        }, 20000);
    }

    public static void allStopService(){

        ComonUtill.applicationContext.stopService(new Intent(ComonUtill.applicationContext, AppMonitorService.class));

        ComonUtill.applicationContext.stopService(new Intent(ComonUtill.applicationContext, LocationService.class));

        ComonUtill.applicationContext.stopService(new Intent(ComonUtill.applicationContext, VideoService.class));

        Intent intent = new Intent(ComonUtill.applicationContext,AudioService.class);
        intent.putExtra("commandType", 2);
        ComonUtill.applicationContext.startService(intent);

    }

}
