package com.recordingapplication.utill;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.google.android.gms.maps.GoogleMap;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.recordingapplication.AppNames;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by KMCC_WIN8 on 2016-05-28.
 */
public final class ComonUtill {


    public static int mRecTime = 30;//30 min

    public static Context applicationContext;
    public static String PREFNAME = "LOCATION_POS_APP";
    public static String max_recording_time = "max_recording_time";
    public static String pre_latitude ="preLatidata";
    public static String pre_longitude ="preLongitdata";
    public static String address ="current_address";


    public static int updateRate = 60 * 1000;
    public static String DirName = "Daddy_cool";
    public static String locfileName = "location.txt";
    public static String monitorfileName = "MonitorApp.txt";


    public static  boolean mIsVideoWorking = false;
    public static  boolean mIsAudioWorking = false;
    public static int CAMERA_TYPE_FRONT = 1;
    public static int CAMERA_TYPE_BACK =0;
    public static int SET_CAMERA = CAMERA_TYPE_FRONT;

    public static boolean bGoogleCheck = false;

    public static boolean bCurRunState = false;

    public static GoogleMap mGoogleMap;


    public static boolean bAllStop = true;

    public static boolean bMapView = false;

    public static String GetNowTime()
    {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strNowDay = sdfNow.format(date);

        return strNowDay;
    }

    public static void saveDBfloat(String recodeName, float savefloat){

        SharedPreferences
                sharedPreferences = applicationContext.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        sharedPreferencesEditor.putFloat(recodeName, savefloat);
        sharedPreferencesEditor.commit();
    }

    public static float getDBfloat(String recodeName){

        SharedPreferences
                sharedPreferences = applicationContext.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        float floatStr = sharedPreferences.getFloat(recodeName, 0.0f);
        return floatStr;
    }

    public static void IntToDB(String prefName, String prefItemKey, int prefItemValue){
        SharedPreferences prefs = applicationContext.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(prefItemKey, prefItemValue);
        editor.commit();
    }

    public static int DBToInt(String prefName, String prefItemKey){
        SharedPreferences prefs = applicationContext.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return prefs.getInt(prefItemKey, 0);
    }

    public static void CreateFolder() {

        File newFolder = new File(Environment.getExternalStorageDirectory(),
                ComonUtill.DirName);
        if (!newFolder.exists()) {
            newFolder.mkdir();
        }

    }

    public static String getName(Context context, AndroidAppProcess process) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = process.getPackageInfo(context, 0);
            return AppNames.getLabel(pm, packageInfo);
        } catch (PackageManager.NameNotFoundException e) {
            return process.name;
        }
    }

    public static Double FloatToDouble(float valFloat){

        String str = String.valueOf(valFloat);

        Double val = Double.parseDouble(str);

        return val;
    }

    public static void saveDBString(String recodeName, String saveStr){

        SharedPreferences
                sharedPreferences = applicationContext.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        sharedPreferencesEditor.putString(recodeName, saveStr);
        sharedPreferencesEditor.commit();
    }

    public static String getDBString(String recodeName){

        SharedPreferences
                sharedPreferences = applicationContext.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        String str = sharedPreferences.getString(recodeName, "");
        return str;
    }

}
