package com.recordingapplication.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.recordingapplication.Global;
import com.recordingapplication.utill.ComonUtill;


/**
 * Service -- Audio Recording
 *********************************************
 */
public class AudioService extends Service {

	private String TAG = "Recorder:";
	private MediaRecorder mediaRecorder = null;
	private String fileName;

	private boolean recording = false;


	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent != null) {

			int commandType = intent.getIntExtra("commandType", 0);

			if (commandType == 1)
				startRecording();
			else if (commandType == 2) {
				stopAndReleaseRecorder(true);
				recording = false;
				this.stopSelf();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void stopAndReleaseRecorder(boolean isNormal) {
		Log.d(TAG, "Recording fininsh");
		if (mediaRecorder == null)
			return;

		try {
			mediaRecorder.stop();
			Thread.sleep(10000);

		} catch (Exception e) {
			Log.e(TAG, "Exception:" + e.getMessage());
			e.printStackTrace();
		} finally {
			mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder = null;
			if(isNormal) {
				if(!fileName.equals(""))
				{
					fileName = "";
					this.stopSelf();
				}
			}
		}

		ComonUtill.mIsAudioWorking = false;
	}

	@Override
	public void onDestroy() {
		stopAndReleaseRecorder(true);
		this.stopSelf();
		super.onDestroy();
	}

	private void startRecording() {

		ComonUtill.mIsAudioWorking = true;



		mediaRecorder = new MediaRecorder();

		try {
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			//ComonUtill.CreateFolder();
			fileName = Environment.getExternalStorageDirectory()
					+ "/" + ComonUtill.DirName + "/" + "audio_" + System.currentTimeMillis() + ".mp4";

			mediaRecorder.setOutputFile(fileName);

			MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
				public void onError(MediaRecorder arg0, int arg1, int arg2) {
					Log.e(TAG, "OnErrorListener " + arg1 + "," + arg2);
					stopAndReleaseRecorder(false);
					recording = false;
				}
			};


			MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
				public void onInfo(MediaRecorder mr, int what, int extra) {
					Log.e(TAG, "OnInfoListener " + what + "," + extra);
					if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {

						if(!fileName.equals(""))
							stopAndReleaseRecorder(true);
						recording = false;

					} else {
						stopAndReleaseRecorder(false);
						recording = false;
					}
				}
			};

			mediaRecorder.setOnErrorListener(errorListener);
			mediaRecorder.setOnInfoListener(infoListener);


			mediaRecorder.setMaxDuration(Global.maxDuration);

			//mediaRecorder.setMaxFileSize(168000000);
			mediaRecorder.prepare();
			Thread.sleep(2000);
			mediaRecorder.start();
			recording = true;

		} catch (Exception e) {
			Log.e(TAG, "Exception");
			e.printStackTrace();
			stopAndReleaseRecorder(false);
			recording = false;
		}
	}

}

