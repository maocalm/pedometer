package com.baisi.spedometer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.baisi.spedometer.step.utils.SharedPreferencesUtils;
import com.baisi.spedometer.utiles.Firebase;
import com.baisi.spedometer.utiles.TouchRectUtils;
import com.bestgo.adsplugin.ads.AdAppHelper;

public class GuideActivity extends AppCompatActivity implements View.OnClickListener {

    private RadioButton mRadioFemal;
    private RadioButton mRadioMale;
    /**
     * NEXT
     */
    private Button mTvNext;
    private LinearLayout check_female;
    private LinearLayout check_male;
    private boolean isFirstIn = true;
    private SharedPreferencesUtils sharedPreferencesUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*加载广告*/
        AdAppHelper.getInstance(getApplicationContext()).loadNewInterstitial();
        AdAppHelper.getInstance(getApplicationContext()).loadNewInterstitial();
        sharedPreferencesUtils = new SharedPreferencesUtils(this);
        if (!(boolean) sharedPreferencesUtils.getParam(SharedPreferencesUtils.FIRST_COMEIN, true)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_guide);
            initView();
            new TouchRectUtils(mRadioFemal, 500);
            new TouchRectUtils(mRadioMale, 500);
        /*默认选中女*/
            mRadioFemal.setChecked(true);
        }
    }

    private void initView() {
        mRadioFemal = (RadioButton) findViewById(R.id.radio_femal);
        //mRadioFemal.setOnClickListener(this);
        mRadioMale = (RadioButton) findViewById(R.id.radio_male);
        //mRadioMale.setOnClickListener(this);
        mTvNext = (Button) findViewById(R.id.tv_next);
        mTvNext.setOnClickListener(this);

        check_female = (LinearLayout) findViewById(R.id.ll_check_female);
        check_male = (LinearLayout) findViewById(R.id.ll_check_male);
        check_female.setOnClickListener(this);
        check_male.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.tv_next:
                /*跳转身高体重设置页*/
                Intent intent = new Intent(this, GuideProfileActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.ll_check_female:
                if (mRadioMale.isChecked()) {
                    mRadioFemal.setChecked(true);
                    mRadioMale.setChecked(false);
                }
                sharedPreferencesUtils.setParam(SharedPreferencesUtils.GENDER, SharedPreferencesUtils.FEMALE);
                    /*同时记录性别*/
                System.out.println("GuideActivity.onClick" + "nv");
                Firebase.getInstance(getApplicationContext()).logEvent("引导页", "性别选择", "female");
                break;

            case R.id.ll_check_male:
                if (mRadioFemal.isChecked()) {
                    mRadioMale.setChecked(true);
                    mRadioFemal.setChecked(false);
                }
                sharedPreferencesUtils.setParam(SharedPreferencesUtils.GENDER, SharedPreferencesUtils.MALE);

                     /*同时记录性别*/
                System.out.println("GuideActivity.onClick" + "nan");
                Firebase.getInstance(getApplicationContext()).logEvent("引导页", "性别选择", "male");
                break;
        }
    }
}
