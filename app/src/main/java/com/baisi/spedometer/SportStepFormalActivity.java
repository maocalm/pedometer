package com.baisi.spedometer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baisi.spedometer.evenbus.Event;
import com.baisi.spedometer.evenbus.EventBusUtil;
import com.baisi.spedometer.utiles.DensityUtil;
import com.baisi.spedometer.view.DampingInterpolator;
import com.baisi.spedometer.view.MixLinearLayout;
import com.bestgo.adsplugin.ads.activity.ShowAdFilter;
import com.fashare.timer_view.DigitalTimerView;
import com.fashare.timer_view.TextViewUpdater;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SportStepFormalActivity extends AppCompatActivity implements ShowAdFilter, View.OnClickListener {
    private String TAG = getClass().getSimpleName();
    private TextView km_value;
    private TextView km_unit_tv;
    private TextView time_count;
    private MixLinearLayout calorle_mix;
    private MixLinearLayout speed_mix;
    private MixLinearLayout step_mix;

    private ImageView pause_start_img;
    private ImageView stop_img;
    private DigitalTimerView digitalTimerView;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_step_formal);
        initView();
        initEvenbus();
        Log.d(TAG, "onCreate: asdfaf");
    }

    private void initView() {
        relativeLayout = findViewById(R.id.animator_rel);
        km_value = findViewById(R.id.km_value);
        km_unit_tv = findViewById(R.id.km_unit_tv);
        //time_count = findViewById(R.id.time_count);
        calorle_mix = findViewById(R.id.calorle_tv);
        speed_mix = findViewById(R.id.speed_tv);
        step_mix = findViewById(R.id.step_tv);
        pause_start_img = findViewById(R.id.pause);
        stop_img = findViewById(R.id.stop);
        pause_start_img.setOnClickListener(this);
        digitalTimerView = findViewById(R.id.dtv_simple);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        digitalTimerView.setSubTimeView(R.layout.item_clock, lp)
                .setSuffixView(R.layout.item_suffix, lp)
                .setViewUpdater(new TextViewUpdater(R.id.tv_time, R.id.tv_suffix));
        //开始计时
        digitalTimerView.start(0);
        EventBusUtil.sendEvent(new Event(EventBusUtil.EventCode.SPORTMODESTART));
    }

    private void initEvenbus() {
        EventBusUtil.register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean allowShowAd() {
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.pause) {
            if (!pause_start_img.isSelected()) { //  pause
                digitalTimerView.pause();
                separateAnimatorSet();
                EventBusUtil.sendEvent(new Event(EventBusUtil.EventCode.SPORTMODESTART) );
            } else { //start
                digitalTimerView.pauseToStart();
                togetherAnimatorSet();
            }

        } else if (id == R.id.stop) {
            digitalTimerView.stop();
        }
    }

    private void separateAnimatorSet() {
        pause_start_img.setImageResource(R.mipmap.pause);
        pause_start_img.setSelected(true);
        stop_img.setVisibility(View.VISIBLE);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator transXLeft = ObjectAnimator.ofFloat(pause_start_img, "translationX", 0, 0 - 270);
        ObjectAnimator transXRight = ObjectAnimator.ofFloat(stop_img, "translationX", 0, 0 + 270);
        transXLeft.setInterpolator(new DampingInterpolator());
        transXRight.setInterpolator(new DampingInterpolator());
        transXLeft.setDuration(700);
        transXRight.setDuration(700);
        animatorSet.playTogether(transXLeft, transXRight);
        animatorSet.start();
        pause_start_img.setOnClickListener(null);
        stop_img.setOnClickListener(null);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                pause_start_img.setOnClickListener(SportStepFormalActivity.this);
                stop_img.setOnClickListener(SportStepFormalActivity.this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    private void togetherAnimatorSet() {
        pause_start_img.setImageResource(R.mipmap.runing);
        pause_start_img.setSelected(false);

        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator transXLeft = ObjectAnimator.ofFloat(pause_start_img, "translationX", 0 - 270, 0);
        ObjectAnimator transXRight = ObjectAnimator.ofFloat(stop_img, "translationX", 0 + 270, 0 + 50);
        transXLeft.setInterpolator(new DampingInterpolator(2, 0.2f));
        //transXRight.setInterpolator(new DampingInterpolator(0, 0.0f));

        //transXLeft.setInterpolator(new BounceInterpolator());
        //transXRight.setInterpolator(new BounceInterpolator());

        transXLeft.setDuration(700);
        transXRight.setDuration(250);
        animatorSet.playTogether(transXLeft, transXRight);
        animatorSet.start();
        pause_start_img.setOnClickListener(null);
        stop_img.setOnClickListener(null);

        transXRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                stop_img.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onAnimationEnd:   right asdfadfaf");

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "onAnimationStart: " + relativeLayout.getWidth());

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "onAnimationEnd: " + relativeLayout.getWidth());
                Log.d(TAG, "onAnimationEnd:   pingmukuan" + DensityUtil.getScreenWidth(SportStepFormalActivity.this));
                pause_start_img.setOnClickListener(SportStepFormalActivity.this);
                stop_img.setOnClickListener(SportStepFormalActivity.this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public  void onEventReceive(Event event ){
        if (event.getCode()== EventBusUtil.EventCode.SPORTMODE_SERVICE_TO_SPORTACTIVITY ){
            // step_mix.
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addCategory(Intent.CATEGORY_HOME);
//                startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        EventBusUtil.unregister(this);
    }
}
