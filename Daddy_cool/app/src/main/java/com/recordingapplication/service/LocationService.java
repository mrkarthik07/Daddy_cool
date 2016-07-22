package com.recordingapplication.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.recordingapplication.Global;
import com.recordingapplication.utill.ComonUtill;
import com.recordingapplication.wxpos.IntergratedLocationManager;
import com.recordingapplication.wxpos.LocationObj;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 *  Location Current Pos Service
 */
public class LocationService extends IntentService {

	String TAG = "Location_tag";

		public LocationService() {
		super(LocationService.class.getSimpleName());
		// TODO Auto-generated constructor stub				
	}
	
	public IntergratedLocationManager er;

	
	public Handler _handlerForStartSearch =  new Handler(){
		@Override
		public void handleMessage(final Message msg){
			switch(msg.what){
			case 0:
				er.getPos();
				_handlerForStartSearch.sendEmptyMessageDelayed(0, ComonUtill.updateRate);
				break;
			case IntergratedLocationManager.ILM_LOCATION_MESSAGE:
				final LocationObj loc = (LocationObj) msg.obj;

				Log.d(TAG, "GPS info test=================================" );
				Log.d(TAG, "Laditude=" + loc._latitude);
				Log.d(TAG, "Longitude=" + loc._longitude);
				Log.d(TAG, "Accuracy=" + loc._acurrcy);
				Log.d(TAG, "Zoom=" + loc._zoom);
				Log.d(TAG, "GPS info test=================================" );

				try{
					float errRange= 0.0001f;

					float current_latitude = loc._latitude;
					float current_longitude = loc._longitude;

					if(Math.abs(ComonUtill.getDBfloat(ComonUtill.pre_latitude) - current_latitude) > errRange
							|| Math.abs(ComonUtill.getDBfloat(ComonUtill.pre_longitude) - current_longitude) > errRange){

						String curAddr = "";
						String content = "";
						if(ComonUtill.bGoogleCheck){
							curAddr = getAddress(current_latitude, current_longitude);
							ComonUtill.saveDBString(ComonUtill.address, curAddr);
							if(!curAddr.equals(""))
								content = rContent(1, current_latitude,current_longitude,curAddr);
							else
								content = rContent(2, current_latitude,current_longitude,curAddr);
						}else
							content = rContent(2, current_latitude,current_longitude,curAddr);


						fileReWrite(content);
						Log.d(">>>>>>>>>>>>>>", content);

						ComonUtill.saveDBfloat(ComonUtill.pre_latitude, current_latitude);
						ComonUtill.saveDBfloat(ComonUtill.pre_longitude, current_longitude);

						ComonUtill.bMapView = true;

					}

				}catch (Exception e){
					e.printStackTrace();
				}

				break;
			}					
		}
	};

	private String rContent(int T, float lat, float lng, String addr){

		String content = "";

		if(T == 1){//address add
			content = (ComonUtill.GetNowTime() + "\t" +
					String.valueOf(lat) + "\t" +
					String.valueOf(lng));
		}else{
			content = (ComonUtill.GetNowTime() + "\t" +
					String.valueOf(lat) + "\t" +
					String.valueOf(lng));
		}
		return content;
	}

	private String getAddress(float lat, float lng){

		String address = getCrrentAddress(ComonUtill.FloatToDouble(lat), ComonUtill.FloatToDouble(lng));

		String curAddress = "";
		if(!address.equals(""))
		{
			String[] arrAddress = address.split("/");
			curAddress = arrAddress[0];
			String preAddress ="";
			for(int i = 1; i < arrAddress.length; i++)
			{
				if(!arrAddress[i].equals("null") && !arrAddress[i].equals(preAddress))
				{
					preAddress = "";
					curAddress += " " + arrAddress[i];
					preAddress = arrAddress[i];
				}
			}
			Log.d("current Addr", "==============================  " + curAddress + "  ==============================");
		}
		return curAddress;
	}

	private void fileReWrite(String wStr){

		File newFolder = new File(Environment.getExternalStorageDirectory(),
				ComonUtill.DirName);
		if (!newFolder.exists()) {
			newFolder.mkdir();
		}

		String fileDirPath = Environment.getExternalStorageDirectory() + "/" + ComonUtill.DirName + "/";

		try{
			BufferedWriter buw = new BufferedWriter(new FileWriter(fileDirPath + ComonUtill.locfileName,true));
			buw.write(wStr, 0, wStr.length());
			buw.newLine();
			buw.close();
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	public String getCrrentAddress(Double lat, Double lng) {

		Geocoder geocoder= new Geocoder(LocationService.this, Locale.US);

		try {
			//Place your latitude and longitude
			List<Address> addresses = geocoder.getFromLocation(lat, lng, 2);
			StringBuilder strAddress = new StringBuilder();

			if(addresses != null && addresses.size() > 0) {

				Address address = addresses.get(0);//

				strAddress.append(address.getCountryName()).append("/");
				strAddress.append(address.getPostalCode()).append("/");
				strAddress.append(address.getLocality()).append("/");
				strAddress.append(address.getSubLocality()).append("/");
				strAddress.append(address.getThoroughfare()).append("/");
				strAddress.append(address.getSubThoroughfare()).append("/");
				strAddress.append(address.getFeatureName());

				Log.d(">>>>>>>>>>>>>>", "================  " + strAddress + "  ================");
				return strAddress.toString();
			}
			else
				return "";
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(er == null)
			er = new IntergratedLocationManager(getApplicationContext(), _handlerForStartSearch);

		_handlerForStartSearch.removeMessages(0);
		_handlerForStartSearch.sendEmptyMessage(0);
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
	}
}
