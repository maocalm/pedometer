package com.baisi.spedometer.pedometer;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class StepsDetectService extends Service {
	
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private StepDetector mStepDetector;
	private String TAG = "StepDetectService";
	
	public static int steps = 0;
	
	@Override
	public void onCreate() {
		
		mStepDetector = new StepDetector();
		registerDetector();
		mStepDetector.setStepListener(new StepListener() {
			@Override
			public void onStep(){
				steps ++;
				if(mOnStepDetectListener != null){
					mOnStepDetectListener.onStepDetect(steps);
				}
			}
		});
		Log.d(TAG, "stepsDetectService  启动");
		Log.d("stepDetect" ,"stepsDetectService");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		steps = 0;
		return super.onStartCommand(intent, flags, startId);
	}

	public void registerDetector() {
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mStepDetector, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
 	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		mOnStepDetectListener = null;
		unRegisterDector();
		steps = 0;
	}
	
	public void unRegisterDector(){
		if(mStepDetector != null && mSensorManager != null){
			mStepDetector.setStepListener(null);
			mSensorManager.unregisterListener(mStepDetector);
		}
	}
	
	public interface OnStepDetectListener {
		public void onStepDetect(int steps);
	}
	
	public static OnStepDetectListener mOnStepDetectListener = null;
	
	public static void setOnStepDetectListener(OnStepDetectListener mListener){
		mOnStepDetectListener = mListener;
	}
}
