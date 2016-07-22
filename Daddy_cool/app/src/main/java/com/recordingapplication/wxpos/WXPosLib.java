package com.recordingapplication.wxpos;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.skyhookwireless.wps.IPLocation;
import com.skyhookwireless.wps.IPLocationCallback;
import com.skyhookwireless.wps.Location;
import com.skyhookwireless.wps.WPSCertifiedLocationCallback;
import com.skyhookwireless.wps.WPSContinuation;
import com.skyhookwireless.wps.WPSLocation;
import com.skyhookwireless.wps.WPSLocationCallback;
import com.skyhookwireless.wps.WPSPeriodicLocationCallback;
import com.skyhookwireless.wps.WPSReturnCode;
import com.skyhookwireless.wps.WPSStreetAddressLookup;
import com.skyhookwireless.wps.XPS;

public class WXPosLib {

	public static String TAG = "WXPosLib";
	public static int DEFAULT_ACCURACY = 300;
	private int m_nAccuracy = DEFAULT_ACCURACY;
	private Context mContext = null;

	public WXPosLib(Context context, Handler handler) {
		mContext = context;
		_parent_handler = handler;
		_streetAddressLookup = WPSStreetAddressLookup.WPS_NO_STREET_ADDRESS_LOOKUP;
		_xps = new XPS(mContext);
		setKey("eJwVwUEOABAMBMCzxzSxZamj0H5K_F3MICF_qkQ6dU1WcArbVkGxLh7DBJGjxzLf5H0OkQsr");
		_stop = true;
	}

	public float getZoomValueFromAccuracy(double accuracy) {
		float zoomval = 8;
		if (accuracy >= 100000)
			zoomval = 5;
		else if (accuracy >= 10000)
			zoomval = 8;
		else if (accuracy >= 1000)
			zoomval = 12;
		else if (accuracy >= 100)
			zoomval = 15;
		else
			zoomval = 17;
		return zoomval;
	}

	private void setKey(String key) {
		if (key.equals(""))
			return;
		try {
			_xps.setKey(key);
		} catch (IllegalArgumentException e) {
			Log.d(TAG,
					"The current API key is invalid. Please update it in settings.");
		}
	}

	public void onClickedW(int accuracy) {
		m_nAccuracy = accuracy;
		Log.d(TAG, "WPS is running...");
		_xps.getLocation(null, _streetAddressLookup, _callback);
		_stop = false;
	}

	public void onClickedX(int accuracy) {
		m_nAccuracy = accuracy;
		Log.d(TAG, "XPS is running...");
		_xps.getXPSLocation(null, 5, 30, _callback);
		_stop = false;
	}
	
	public void stop() {
		_xps.abort();
		_stop = true;
	}

	private class MyLocationCallback implements IPLocationCallback,
			WPSLocationCallback, WPSPeriodicLocationCallback,
			WPSCertifiedLocationCallback {
		public void done() {
			// tell the UI thread to re-enable the buttons
			_handler.sendMessage(_handler.obtainMessage(DONE_MESSAGE));
		}

		public WPSContinuation handleError(final WPSReturnCode error) {
			// send a message to display the error
			_handler.sendMessage(_handler.obtainMessage(ERROR_MESSAGE, error));
			// return WPS_STOP if the user pressed the Stop button
			if (!_stop)
				return WPSContinuation.WPS_CONTINUE;
			else
				return WPSContinuation.WPS_STOP;
		}

		public void handleIPLocation(final IPLocation location) {
			// send a message to display the location
			_handler.sendMessage(_handler.obtainMessage(LOCATION_MESSAGE,
					location));
		}

		public void handleWPSLocation(final WPSLocation location) {
			// send a message to display the location
			_handler.sendMessage(_handler.obtainMessage(LOCATION_MESSAGE,
					location));
		}

		public WPSContinuation handleWPSPeriodicLocation(
				final WPSLocation location) {
			_handler.sendMessage(_handler.obtainMessage(LOCATION_MESSAGE,
					location));
			// return WPS_STOP if the user pressed the Stop button
			if (!_stop)
				return WPSContinuation.WPS_CONTINUE;
			else
				return WPSContinuation.WPS_STOP;
		}

		public WPSContinuation handleWPSCertifiedLocation(
				final WPSLocation[] locations) {
			_handler.sendMessage(_handler.obtainMessage(LOCATION_LIST_MESSAGE,
					locations));
			// return WPS_STOP if the user pressed the Stop button
			if (!_stop)
				return WPSContinuation.WPS_CONTINUE;
			else
				return WPSContinuation.WPS_STOP;
		}
	}

	// our Handler understands six messages
	public static final int LOCATION_MESSAGE = 1;
	public static final int ERROR_MESSAGE = 2;
	public static final int DONE_MESSAGE = 3;
	public static final int REGISTRATION_SUCCESS_MESSAGE = 4;
	public static final int REGISTRATION_ERROR_MESSAGE = 5;
	public static final int LOCATION_LIST_MESSAGE = 6;
	public static final int ERROR_ACCURACY = 7;

	Handler _handler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			_stop = true;
			switch (msg.what) {
			case LOCATION_MESSAGE:
				String strObj = ((Location) msg.obj).toString();
				int firstindex = strObj.indexOf("+/-");
				int lastindex = strObj.indexOf("m", firstindex);

				String strAccuracy = strObj
						.substring(firstindex + 3, lastindex);

				int accuracy = DEFAULT_ACCURACY;
				try {
					accuracy = Integer.parseInt(strAccuracy.trim());
				} catch (Exception e) {
					accuracy = DEFAULT_ACCURACY;
				}
				_loc._acurrcy = accuracy;

				String longtideStr = String.valueOf(((Location) msg.obj).getLatitude());
				String latitudeStr = String.valueOf(((Location) msg.obj).getLongitude());


				_loc._latitude = Float.parseFloat(longtideStr);
				_loc._longitude = Float.parseFloat(latitudeStr);

				_loc._zoom = (int)getZoomValueFromAccuracy((double)accuracy);
				if (accuracy > m_nAccuracy) {
					Log.d(TAG, "");
					_xps.abort();
					_parent_handler.sendMessage(_handler.obtainMessage(ERROR_ACCURACY, _loc));
					return;
				}
				_parent_handler.sendMessage(_handler.obtainMessage(LOCATION_MESSAGE, _loc));
				_xps.abort();
				Log.d(TAG, ((Location) msg.obj).toString());////////////////////////////////////////////////////////////////////
				return;
			case ERROR_MESSAGE:
				_parent_handler.sendMessage(_handler.obtainMessage(ERROR_MESSAGE, null));
				Log.d(TAG, "");
				_xps.abort();
				return;
			case DONE_MESSAGE:
				_stop = false;
				return;
			case REGISTRATION_SUCCESS_MESSAGE:
				Log.d(TAG, "Registration succeeded");
				return;
			case REGISTRATION_ERROR_MESSAGE:
				Log.d(TAG, "Registration failed ("
						+ ((WPSReturnCode) msg.obj).name() + ")");
				return;
			}
		}
	};

	private final MyLocationCallback _callback = new MyLocationCallback();
	private boolean _stop;
	private LocationObj _loc = new LocationObj();
	private XPS _xps;
	private WPSStreetAddressLookup _streetAddressLookup;
	private Handler _parent_handler;
}
