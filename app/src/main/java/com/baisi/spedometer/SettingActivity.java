package com.baisi.spedometer;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    /**
     * Female
     */
    private TextView mTvShowGender;
    private RelativeLayout mRlGender;
    /**
     * 164cm
     */
    private TextView mTvShowHeight;
    private RelativeLayout mRlHeight;
    /**
     * 53.0kg
     */
    private TextView mTvShowWeight;
    private RelativeLayout mRlWeight;
    private TextView mTvShowMetric;
    private RelativeLayout mRlMetric;
    /**
     * 6000
     */
    private TextView mTvShowGoal;
    private RelativeLayout mRlSetpGoal;
    private SwitchCompat mSwc;
    private RelativeLayout mRlNott;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //initView();
        //setToolbar();

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void initView() {
        //mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTvShowGender = (TextView) findViewById(R.id.tv_show_gender);
        mRlGender = (RelativeLayout) findViewById(R.id.rl_gender);
        mTvShowHeight = (TextView) findViewById(R.id.tv_show_height);
        mRlHeight = (RelativeLayout) findViewById(R.id.rl_height);
        mTvShowWeight = (TextView) findViewById(R.id.tv_show_weight);
        mRlWeight = (RelativeLayout) findViewById(R.id.rl_weight);
        mTvShowMetric = (TextView) findViewById(R.id.tv_show_metric);
        mRlMetric = (RelativeLayout) findViewById(R.id.rl_metric);
        mTvShowGoal = (TextView) findViewById(R.id.tv_show_goal);
        mRlSetpGoal = (RelativeLayout) findViewById(R.id.rl_setp_goal);
        mSwc = (SwitchCompat) findViewById(R.id.swc);
        mRlNott = (RelativeLayout) findViewById(R.id.rl_nott);
    }

    private void setToolbar() {
        setSupportActionBar(mToolbar);
        //设置toolbar后调用setDisplayHomeAsUpEnabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(0);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
