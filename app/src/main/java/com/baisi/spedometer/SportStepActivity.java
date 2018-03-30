package com.baisi.spedometer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.bestgo.adsplugin.ads.activity.ShowAdFilter;

public class SportStepActivity extends AppCompatActivity implements ShowAdFilter {

    private TextView sport_time;
    private int halfHeightPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_step);
        initView();
        startAnimotion() ;
    }

    private void initView() {
        sport_time = findViewById(R.id.time_dao);
    }

    private void  startAnimotion(){
        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        halfHeightPixels = heightPixels/2;
        MyCountDown myCountDown =new MyCountDown(4000, 1000);
        myCountDown.start() ;
    }

    @Override
    public boolean allowShowAd() {
        return false;
    }

    class MyCountDown extends CountDownTimer {


        public MyCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            sport_time.setText(millisUntilFinished/1000+"");
            ObjectAnimator alphaAnimIn = ObjectAnimator.ofFloat(sport_time ,"alpha" ,0.2f ,1f );
            ObjectAnimator transYAnimIn = ObjectAnimator.ofFloat(sport_time ,"translationY" ,0-400 ,0);
            transYAnimIn.setInterpolator(new AccelerateDecelerateInterpolator());

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ObjectAnimator alphaAnimEx = ObjectAnimator.ofFloat(sport_time ,"alpha" ,1f ,0f );
                    ObjectAnimator transYAnimEx = ObjectAnimator.ofFloat(sport_time ,"translationY" ,0 ,0+400);
                    transYAnimEx.setInterpolator(new AccelerateInterpolator());
                    AnimatorSet  animatorSetEx = new AnimatorSet();
                    animatorSetEx.playTogether(alphaAnimEx ,transYAnimEx );//,alphaAnimEx ,transYAnimEx);
                    animatorSetEx.setDuration(500);
                    animatorSetEx.start();

                    animatorSetEx.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {

                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                }
            },500);

            AnimatorSet  animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimIn ,transYAnimIn );//,alphaAnimEx ,transYAnimEx);
            animatorSet.setDuration(500);
            animatorSet.start();



        }

        @Override
        public void onFinish() {
            finish();
            Intent intent = new Intent(SportStepActivity.this ,SportStepFormalActivity.class);
            startActivity(intent);
        }
    }
}
