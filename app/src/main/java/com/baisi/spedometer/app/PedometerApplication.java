package com.baisi.spedometer.app;

import android.app.Application;

import com.baisi.spedometer.utiles.FacebookAnalytics;
import com.baisi.spedometer.utiles.Firebase;
import com.bestgo.adsplugin.ads.AdAppHelper;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import io.fabric.sdk.android.Fabric;

public class PedometerApplication extends Application {

    private static volatile PedometerApplication singleton;

    public PedometerApplication() {
    }

    public static PedometerApplication getInstance() {
        if (singleton == null) {
            synchronized (PedometerApplication.class) {
                if (singleton == null) {
                    singleton = new PedometerApplication();
                }
            }
        }
        return singleton;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());  //初始化facebook sdk
        AppEventsLogger.activateApp(this);   //开启facebook应用分析
        Fabric.with(this, new Crashlytics());
        AdAppHelper.FACEBOOK = FacebookAnalytics.getInstance(getApplicationContext());
        AdAppHelper.FIREBASE = Firebase.getInstance(this);
        AdAppHelper.getInstance(getApplicationContext()).init();
    }

}