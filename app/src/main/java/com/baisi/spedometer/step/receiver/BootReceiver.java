package com.baisi.spedometer.step.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baisi.spedometer.step.service.StepService;

/**
 * Created by hanwenmao on 2018/1/8.
 */

public class BootReceiver extends BroadcastReceiver {
    private  String TAG =getClass().getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context , StepService.class));
        Log.d(TAG, "onReceive: ");
    }
}
