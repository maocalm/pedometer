package com.baisi.spedometer;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bestgo.adsplugin.ads.AdAppHelper;
import com.bestgo.adsplugin.ads.activity.ShowAdFilter;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class FlickePageActivity extends AppCompatActivity implements ShowAdFilter {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flicke_page);

        AdAppHelper.getInstance(getApplicationContext()).loadNewInterstitial();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                Intent intent = new Intent(FlickePageActivity.this, GuideActivity.class);
                startActivity(intent);
            }
        }, 3000);


    }

    @Override
    public boolean allowShowAd() {
        return false;
    }
}
