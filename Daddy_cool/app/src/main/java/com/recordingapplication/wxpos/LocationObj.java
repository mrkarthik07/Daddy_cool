package com.recordingapplication.wxpos;

public class LocationObj {
	public float _latitude;
	public float _longitude;
	public int _acurrcy;
	public int _zoom;
	
	public LocationObj() {
		_latitude = 0.0f;
		_longitude = 0.0f;
		_acurrcy = WXPosLib.DEFAULT_ACCURACY;
		_zoom = 15;		
	}
}
