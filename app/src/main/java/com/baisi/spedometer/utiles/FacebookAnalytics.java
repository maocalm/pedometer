package com.baisi.spedometer.utiles;

import android.content.Context;

import com.bestgo.adsplugin.ads.analytics.AbstractFacebook;
import com.facebook.appevents.AppEventsLogger;

public class FacebookAnalytics extends AbstractFacebook {

    private AppEventsLogger mLogger;
    private Context mContext;
    private static FacebookAnalytics instance;

    public FacebookAnalytics(Context context) {
        this.mContext = context;
    }

    public static FacebookAnalytics getInstance(Context context) {
        if (instance == null) {
            instance = new FacebookAnalytics(context);
        }
        return instance;
    }

    private AppEventsLogger getLogger() {
        if (mLogger == null) {
            try {
                mLogger = AppEventsLogger.newLogger(mContext);
            } catch (Exception e) {

            }
        }
        return mLogger;
    }

    @Override
    public void logEvent(String name, double value) {
        getLogger().logEvent(name, value);
    }
}
