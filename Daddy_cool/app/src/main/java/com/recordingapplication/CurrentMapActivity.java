package com.recordingapplication;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.recordingapplication.utill.ComonUtill;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CurrentMapActivity extends Activity {

    private boolean blineStart = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        float latitude = ComonUtill.getDBfloat(ComonUtill.pre_latitude);
        float longitude = ComonUtill.getDBfloat(ComonUtill.pre_longitude);

        LatLng pos = new LatLng( latitude, longitude);

        ComonUtill.mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        Marker curMarker = ComonUtill.mGoogleMap.addMarker(new MarkerOptions().position(pos)
                .title(ComonUtill.getDBString(ComonUtill.address)));

        ComonUtill.mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom( pos, 15));

        ComonUtill.mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        try {
            onTextRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onTextRead() throws IOException {

        String path = Environment.getExternalStorageDirectory() + "/" + ComonUtill.DirName + "/" + ComonUtill.locfileName;
        FileInputStream fis = new FileInputStream(path);
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));

        String str;
        String preArr[] = new String[2];
        String curArr[] ;
        boolean first = true;
        while( (str = bufferReader.readLine()) != null ) {

            if(first)
                preArr = str.split("\t");
            else
            {
                curArr = str.split("\t");

                float prelati = StringToFloat(preArr[1]);
                float prelogi = StringToFloat(preArr[2]);

                float curlati = StringToFloat(curArr[1]);
                float curlogi = StringToFloat(curArr[2]);

                preArr = curArr;
                LatLng prepos = new LatLng( prelati, prelogi);
                LatLng curpos = new LatLng( curlati, curlogi);

                mapLine(prepos,curpos);
            }
            first = false;
        }

    }

    private float StringToFloat(String str){
        float sfloat = Float.parseFloat(str);
        return sfloat;
    }

    private void mapLine(LatLng pre_pos, LatLng cur_pos){

        if(blineStart) {
            Marker startMarker = ComonUtill.mGoogleMap.addMarker(new MarkerOptions().position(pre_pos)
                    .title("Start"));
        }

        ComonUtill.mGoogleMap.addPolyline(new PolylineOptions().add(pre_pos, cur_pos).width(8).color(Color.RED));

        blineStart = false;
    }

}
