package com.recordingapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.recordingapplication.service.AppMonitorService;
import com.recordingapplication.utill.ComonUtill;

import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener{

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private Spinner spinner = null;
    private int spinPos = 0 ;
    private RadioButton mMonitor,mLocation;
    private boolean bMonitor = false,bLocation = false;
    private Button mStart,mMapview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Global.gCurrentActivity = this;

        layoutInit();

        if(ComonUtill.bCurRunState)
            mStart.setText("Stop");
        else
            mStart.setText("Start");

//        ComonUtill.bGoogleCheck = checkPlayServices();
        ComonUtill.bGoogleCheck = true;
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(MainActivity.this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, MainActivity.this,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                Log.d("TAG", "bGoogleCheck : false");
                finish();
            }
            return false;
        }
        return true;
    }
    private void layoutInit(){

        ArrayList arraylist = new ArrayList();

        arraylist.add("Video record");
        arraylist.add("Audio record");

        spinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter spinAdapter = new ArrayAdapter(
                this,
                R.layout.spin,
                arraylist);
        spinAdapter.setDropDownViewResource(R.layout.spin_dropdown);

        spinner.setAdapter(spinAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                spinPos = 0;
                spinPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        TextView tvPath = (TextView) findViewById(R.id.tv_path);
        String strPath = "Save Path : \n" + Environment.getExternalStorageDirectory() + "/" + ComonUtill.DirName;
        tvPath.setText(strPath);


        mStart = (Button) findViewById(R.id.btn_start);
        mStart.setOnClickListener(this);

        mMonitor = (RadioButton) findViewById(R.id.radio_monitor);
        mMonitor.setOnClickListener(this);
        mMonitor.setChecked(true);
        bMonitor = true;

        mLocation = (RadioButton) findViewById(R.id.radio_location);
        mLocation.setOnClickListener(this);
        mLocation.setChecked(true);
        bLocation= true;


        mMapview = (Button) findViewById(R.id.btn_map_view);
        mMapview.setOnClickListener(this);

        if(ComonUtill.bMapView)
            mMapview.setVisibility(View.VISIBLE);
        else
            mMapview.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.radio_monitor:
                if(bMonitor){
                    mMonitor.setChecked(false);
                    bMonitor = false;
                }else{
                    mMonitor.setChecked(true);
                    bMonitor = true;
                }
                break;

            case R.id.radio_location:
                if(bLocation){
                    mLocation.setChecked(false);
                    bLocation = false;
                }else{
                    mLocation.setChecked(true);
                    bLocation = true;
                }
                break;

            case R.id.btn_start:
//                if(runStart(spinPos,bMonitor,bLocation))
                    runStart(spinPos,bMonitor,bLocation);
                    MainActivity.this.finish();
                break;

            case R.id.btn_map_view:
                if(ComonUtill.bMapView){
                    Intent intent  = new Intent(ComonUtill.applicationContext, CurrentMapActivity.class);
                    startActivity(intent);
                }
                else
                    Toast.makeText(ComonUtill.applicationContext,"Please click the Start button, and then wait one minute.",Toast.LENGTH_LONG).show();

                break;
        }
    }

    private boolean runStart(int rectype, boolean monitor,boolean location){

        boolean result = false;

        if(ComonUtill.bCurRunState) {
            ComonUtill.bCurRunState = false;
            ComonUtill.bAllStop = true;
            Global.allStopService();
            mStart.setText("Start");
        }else{

            ComonUtill.bAllStop = false;

            ComonUtill.CreateFolder();

            Global.StartRecording(rectype);

            if(monitor)
                Global.StartAppMonitor();

            if(location)
                Global.StartLocation();

            ComonUtill.bCurRunState = true;
            result = true;
        }

        return result;

    }
}
