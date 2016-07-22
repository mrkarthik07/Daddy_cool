package com.recordingapplication.wxpos;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;



public class IntergratedLocationManager {
	private static final String TAG = "ILManager";

	public static final int ILM_SW_LOCATION_MESSAGE = 18;
	public static final int ILM_WX_LOCATION_MESSAGE = WXPosLib.LOCATION_MESSAGE;
	public static final int ILM_WX_ERROR_ACCURACY = WXPosLib.ERROR_ACCURACY;
	public static final int ILM_LOC_ERROR = WXPosLib.ERROR_MESSAGE;
	public static final int ILM_LOCATION_MESSAGE = 20;


	public static int DEFAULT_ACCURACY = 1000;
	public static int DEFAULT_TIMEOUT = 60000;
	public static int DEFAULT_ZOOM = 15;

	private static int failedCount = 0;

	private WXPosLib _wxPos = null;

	private Handler mParentHandler;

	Handler mHandler;

	LocationManager gpsManager;
	GPSLocationListener gpsListener;
	Context mContext;


	public IntergratedLocationManager(Context context, Handler parentHandler) {
		mContext = context;
		mHandler = new Handler( new LocationHandlerCallBack());
		mParentHandler = parentHandler;
		_wxPos = new WXPosLib(mContext, mHandler);
		gpsListener = new GPSLocationListener();


	}

	public void getPos() {
		if (gpsManager == null) {
			
			gpsManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		}
		
		try {
			if (failedCount >= 3) {
				_wxPos.onClickedW(DEFAULT_ACCURACY);
			} else if (gpsManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				gpsManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DEFAULT_TIMEOUT, DEFAULT_ACCURACY, gpsListener);
			} else if (gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				gpsManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_TIMEOUT, DEFAULT_ACCURACY, gpsListener);
			} else {
				_wxPos.onClickedW(DEFAULT_ACCURACY);
			}
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
		failedCount ++;
	}

	public void stop() {
		_wxPos.stop();
	}


	class GPSLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			LocationObj loc = new LocationObj();
			String longtideStr = String.valueOf(location.getLongitude());
			String latitudeStr = String.valueOf(location.getLatitude());
			loc._longitude =Float.parseFloat(longtideStr);
			loc._latitude = Float.parseFloat(latitudeStr);
			loc._acurrcy = (int) location.getAccuracy();
			loc._zoom = IntergratedLocationManager.DEFAULT_ZOOM;

			if ((int) location.getAccuracy() < DEFAULT_ACCURACY) {
				mParentHandler.sendMessage(mParentHandler.obtainMessage(ILM_LOCATION_MESSAGE, loc));

			} else {
				mParentHandler.sendMessage(mParentHandler.obtainMessage(ILM_WX_ERROR_ACCURACY, loc));
			}

			failedCount = 0;


//			Log.d(TAG, "GPS : lat" + loc._latitude + "/lon=" + loc._longitude + "/acu=" + loc._acurrcy);
//
//
//			String pre_latitude = String.valueOf(loc._latitude);
//			String pre_longitude = String.valueOf(loc._longitude);
//
//			if(!ComonUtill.getDBString(ComonUtill.pre_latitude).equals(pre_latitude) || !ComonUtill.getDBString(ComonUtill.pre_longitude).equals(pre_longitude)){
//
//
//				String content = (ComonUtill.GetNowTime() + " : Latitude - [" + pre_latitude + "] " + " Longitude - [" + pre_longitude + "]");
//				fileReWrite(content);
//
//				ComonUtill.saveDBString(ComonUtill.pre_latitude, pre_latitude);
//				ComonUtill.saveDBString(ComonUtill.pre_longitude, pre_longitude);
//			}
		}

//		private void fileReWrite(String wStr){
//
//			File newFolder = new File(Environment.getExternalStorageDirectory(),
//					ComonUtill.dirName);
//			if (!newFolder.exists()) {
//				newFolder.mkdir();
//			}
//
//			String fileDirPath = Environment.getExternalStorageDirectory() + "/" + ComonUtill.dirName + "/";
//
//			try{
//				BufferedWriter buw = new BufferedWriter(new FileWriter(fileDirPath + ComonUtill.fileName,true));
//				buw.write(wStr, 0, wStr.length());
//				buw.newLine();
//				buw.close();
//			} catch(IOException e){
//				e.printStackTrace();
//			}
//		}

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}

	}


	class LocationHandlerCallBack implements Handler.Callback {
		@Override
		public boolean handleMessage(Message msg) {
			LocationObj loc = new LocationObj();
			loc = (LocationObj) msg.obj;
			failedCount = 0;
			switch (msg.what) {

			case ILM_SW_LOCATION_MESSAGE: {
				Bundle bundle = msg.getData();
				double latitude = bundle.getDouble("latitude");
				double longitude = bundle.getDouble("longitude");
				double accuracy = bundle.getDouble("accuracy");

				LocationObj locSW = new LocationObj();

				String longtideStr = String.valueOf(latitude);
				String latitudeStr = String.valueOf(longitude);
				locSW._latitude = Float.parseFloat(longtideStr);
				locSW._longitude = Float.parseFloat(latitudeStr);
				locSW._acurrcy = (int) accuracy;
				locSW._zoom = IntergratedLocationManager.DEFAULT_ZOOM;

				if (accuracy < IntergratedLocationManager.DEFAULT_ACCURACY) {
					mParentHandler.sendMessage(mParentHandler.obtainMessage(ILM_LOCATION_MESSAGE, locSW));

				} else {
					mParentHandler.sendMessage(mParentHandler.obtainMessage(ILM_WX_ERROR_ACCURACY, locSW));
				}

				break;
			}

			case ILM_WX_LOCATION_MESSAGE:
				mParentHandler.sendMessage(mParentHandler.obtainMessage(ILM_LOCATION_MESSAGE, loc));

				break;
			case ILM_WX_ERROR_ACCURACY:
				mParentHandler.sendMessage(mParentHandler.obtainMessage(ILM_WX_ERROR_ACCURACY, loc));
				break;
			case ILM_LOC_ERROR:
				Log.d(TAG, "WX : Network error");
				break;
			default:
				break;
			}
			return true;
		}
	};


}
