package com.baisi.spedometer.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.MainThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baisi.spedometer.HistoryActivity;
import com.baisi.spedometer.R;
import com.baisi.spedometer.WeekReportActivity;
import com.baisi.spedometer.base.BaseFragment;
import com.baisi.spedometer.evenbus.Event;
import com.baisi.spedometer.evenbus.EventBusUtil;
import com.baisi.spedometer.fragment.fragmentlistenner.PedometerFragmentListener;
import com.baisi.spedometer.greendao.gen.DaoSession;
import com.baisi.spedometer.greendao.gen.StepDataDao;
import com.baisi.spedometer.step.UpdateUiCallBack;
import com.baisi.spedometer.step.bean.StepData;
import com.baisi.spedometer.step.service.StepService;
import com.baisi.spedometer.step.utils.MyDbUtils;
import com.baisi.spedometer.step.utils.SharedPreferencesUtils;
import com.baisi.spedometer.step.utils.StepConversion;
import com.baisi.spedometer.utiles.DensityUtil;
import com.baisi.spedometer.view.ProgressRing;
import com.baisi.spedometer.view.TextViewDrawable;
import com.baisi.spedometer.view.floataction.FloatingActionButton;
import com.baisi.spedometer.view.floataction.FloatingActionMenu;
import com.bestgo.adsplugin.ads.activity.ShowAdFilter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by MnyZhao on 2017/11/14.
 */

public class PedometerFragment extends BaseFragment implements View.OnClickListener ,ShowAdFilter {

    private View view;
    private boolean isBind = false;
    private TextView step_in_progress;
    private ProgressRing progressRing;
    private TextView setp_goal;
    private TextViewDrawable pause_resume;
    private SharedPreferencesUtils mSpUtils;
    private String mStepGoal;  //目标步数
    private int stepCount; // 当天步数；
    private TextView pause_in_progress;
    private StepService stepService;

    private String TAG = "pedometer";

    /*体重*/
    private Float weght;
    private TextView kcal_tv;
    private TextView km_tv;
    private TextView time_tv;
    private String gender;
    private int height;
    private FloatingActionMenu menuDown;
    private MyDbUtils myDbUtils;
    private FloatingActionButton timeline_float;
    private FloatingActionButton reset_float;
    private DaoSession daoSession;

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.pedometer_fragment, null);
        Log.d(TAG, "  screen____ height_dip_" + DensityUtil.getDmDensityDpi());

        //if (!judGmentGoogleFit()) {
         /*开启计步服务*/
        startStepService();
        //}

        step_in_progress = (TextView) view.findViewById(R.id.steps_in_progress);
        progressRing = (ProgressRing) view.findViewById(R.id.ProgressRing);
        pause_resume = (TextViewDrawable) view.findViewById(R.id.pause_resume);
        pause_in_progress = (TextView) view.findViewById(R.id.pause_in_progress);
        setp_goal = (TextView) view.findViewById(R.id.setp_goal_in_progress);

        kcal_tv = (TextView) view.findViewById(R.id.kcal);
        km_tv = (TextView) view.findViewById(R.id.km);
        time_tv = (TextView) view.findViewById(R.id.time);

        kcal_tv.setOnClickListener(this);
        km_tv.setOnClickListener(this);
        time_tv.setOnClickListener(this);

        pause_resume.setOnClickListener(this);


        mSpUtils = new SharedPreferencesUtils(getActivity());
        mStepGoal = mSpUtils.getParam(SharedPreferencesUtils.STEP_GOAL, SharedPreferencesUtils.STEPGOAL_DEFAULT).toString();

        // view.findViewById(R.id.pause_resume).setSelected(true);
        weght = (Float) mSpUtils.getParam(SharedPreferencesUtils.WEIGHT, SharedPreferencesUtils.WEIGHT_DEFAULT);
        gender = (String) mSpUtils.getParam(SharedPreferencesUtils.GENDER, SharedPreferencesUtils.GENDER_DEFAULT);
        height = (int) mSpUtils.getParam(SharedPreferencesUtils.HEIGHT, SharedPreferencesUtils.Height_DEFAULT);

        myDbUtils = new MyDbUtils(getActivity());
        daoSession = myDbUtils.getDaoManager().getDaoSession();
        // 悬浮按钮
        //floatSetting();
        EventBusUtil.register(this);
        initProgress();
        initKmKcTime();
        Log.d(TAG, "initView >>>>>");
        return view;
    }



    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        //super.setUserVisibleHint(isVisibleToUser);
        if (view != null) {
            Log.d(TAG, "onvisible ");
        }
    }

    private boolean judGmentGoogleFit() {

        final FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                        .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT)

                        .build();


        if (GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getActivity()), fitnessOptions)) {
            //readDat
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "on resume ");
        mStepGoal = (String) mSpUtils.getParam(SharedPreferencesUtils.STEP_GOAL, SharedPreferencesUtils.STEPGOAL_DEFAULT);
        //setp_goal.setText(Html.fromHtml(String.format(getString(R.string.goal_with_value), mStepGoal + "")));
        setp_goal.setText(String.format(getString(R.string.goal_with_value), mStepGoal));
        // setProgressRing(stepCount);
    }

    @Override
    protected void initData() {
        /*
        * 设置返回重新设置 stepgoal
        * */
        mStepGoal = (String) mSpUtils.getParam(SharedPreferencesUtils.STEP_GOAL, SharedPreferencesUtils.STEPGOAL_DEFAULT);
        //setp_goal.setText(Html.fromHtml(String.format(getString(R.string.html_goal), mStepGoal + "")));
        setp_goal.setText(String.format(getString(R.string.goal_with_value), mStepGoal));


    }

    private void initKmKcTime() {
        /*根据步数计算distance  kcal */
        String kcal = StepConversion.getCalorie(weght, stepCount);
        kcal_tv.setText(String.valueOf(kcal));

        String distance = StepConversion.getDistance(gender, stepCount, height);
        km_tv.setText(distance);

        /*时长-- 分钟*/
        time_tv.setText(String.valueOf(getStepSecond() / 60));
    }

    private void initProgress() {

        /*第一次进去默认开始*/
        setPause_resume(true);
        //setp_goal.setText(Html.fromHtml(String.format(getString(R.string.html_goal), mStepGoal + "")));
        setp_goal.setText(String.format(getString(R.string.goal_with_value), mStepGoal));

        int stepcount = getStepCount();
        step_in_progress.setText(String.valueOf(stepcount));
        setProgressRing(stepcount);


    }

    private int getStepSecond() {
        int stepSecond = 0;
        List<StepData> stepDataList = myDbUtils.getDaoManager()
                .getDaoSession().getStepDataDao().queryBuilder()
                .where(StepDataDao.Properties.StepToday.eq(getTodaySimpleDate()), StepDataDao.Properties.Reset.eq("false"))
                .list();
        if (stepDataList.size() == 0 || stepDataList.isEmpty()) {
            stepSecond = 0;
        } else if (stepDataList.size() == 1) {
            Log.v(TAG, "StepData=" + stepDataList.get(0).toString());
            stepSecond = stepDataList.get(0).getStepSecond();
        }
        return stepSecond;
    }

    public int getStepCount() {
        List<StepData> stepDataList = myDbUtils.getDaoManager().getDaoSession().getStepDataDao().
                queryBuilder().where(StepDataDao.Properties.StepToday.eq(getTodaySimpleDate()), StepDataDao.Properties.Reset.eq("false")).list();
        if (stepDataList.size() == 0 || stepDataList.isEmpty()) {
            stepCount = 0;
        }
        for (StepData stepdata : stepDataList) {
            stepCount += stepdata.getStep();
        }
        return stepCount;
    }

    public void setStepGoalTextView(String stepgoal) {
        if (setp_goal == null) {
            Log.d(TAG, "step_goal == null ");
            return;
        }
        mStepGoal = stepgoal;
        setp_goal.setText(String.format(getString(R.string.goal_with_value), mStepGoal));
        setProgressRing(Integer.valueOf(getStepCount()));
        Log.d(TAG, "stepcount ----- stepgoal " + getStepCount() + "------" + stepgoal);
    }

    /**
     * 获取当天日期
     *
     * @return
     */
    private String getTodaySimpleDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    @Override
    protected void setDefaultFragmentTitle(String title) {
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), WeekReportActivity.class);
        String type = "TYPE";
        switch (v.getId()) {
            case R.id.pause_resume:
                setPause_resume(!pause_resume.isSelected());
                break;
            case R.id.kcal:
                intent.putExtra(type, "kcal");
                startActivity(intent);
                break;
            case R.id.km:
                intent.putExtra(type, "distance");
                startActivity(intent);
                EventBusUtil.sendStickyEvent(new Event(EventBusUtil.EventCode.GOWEEKACTIVITY, "distance"));
                break;
            case R.id.time:
                intent.putExtra(type, "time");
                startActivity(intent);
                EventBusUtil.sendStickyEvent(new Event(EventBusUtil.EventCode.GOWEEKACTIVITY, "time"));
                break;
        }
    }

    private static PedometerFragmentListener pedometerFragmentListener;

   /* public interface PedometerFragmentListener {
        void setCurrent(int fragment,String tag);
    }*/

    public void registerPedometerFragmentListener(PedometerFragmentListener pedometerFragmentListener) {
        this.pedometerFragmentListener = pedometerFragmentListener;
    }

    /*停止 ，开始计步*/
    private void setPause_resume(boolean isOrNo) {
        pause_resume.setSelected(isOrNo);
        pause_in_progress.setVisibility(View.GONE);
        pause_resume.setText(R.string.pause);
        if (stepService != null) {  //  stepservice  只有再回调传回来的时候才不为空；
            if (isOrNo) {
                Log.d(TAG, "setPause_resume:  true ");
                pause_in_progress.setVisibility(View.GONE);
                pause_resume.setText(R.string.pause);
                stepService.registerReceiver();

            } else {
            /*停止计步*/
                pause_in_progress.setVisibility(View.VISIBLE);
                pause_resume.setText(R.string.resume);
                //new StepCount().setOffSteps(false);
                stepService.unRegisterReceiver();
            }
        }
    }

    private void startStepService() {
        Intent intent = new Intent(getActivity(), StepService.class);
        getActivity().startService(intent);
        isBind = getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        getActivity().startService(intent);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            stepService = ((StepService.StepBinder) service).getService();
            Log.d(TAG, "pedometer  onserviceconnected ");
            //设置步数监听回调
            stepService.registerCallback(new UpdateUiCallBack() {
                @Override
                public void updateUi(int stepCount, int stepTimeCountSecond) {

                    Log.d(TAG, "pedometer  updateUi");
                    //设置ui步数
                    step_in_progress.setText(stepCount + "");

                    setProgressRing(mStepGoal, stepCount);

                    setDistancekcalTime(stepCount, stepTimeCountSecond);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    /**
     * @param stepTimeCountSecond 回调的时长；
     */
    private void setDistancekcalTime(int stepCount, int stepTimeCountSecond) {
        /*根据步数计算distance  kcal */
        String kcal = StepConversion.getCalorie(weght, stepCount);
        kcal_tv.setText(String.valueOf(kcal));

        String distance = StepConversion.getDistance(gender, stepCount, height);
        km_tv.setText(distance);

                    /*时长-- 分钟*/
        time_tv.setText(String.valueOf(stepTimeCountSecond / 60));
    }


    /**
     * 设置进度条
     *
     * @param mStepGoal
     * @param stepCount
     */
    private void setProgressRing(String mStepGoal, int stepCount) {
        if (mStepGoal != "0") {
            progressRing.setProgress(stepCount * 100 / Integer.parseInt(mStepGoal));
        } else {
            progressRing.setProgress(100);
        }
    }

    private void setProgressRing(int stepCount) {
        String mStepGoal = mSpUtils.getParam(SharedPreferencesUtils.STEP_GOAL, SharedPreferencesUtils.STEPGOAL_DEFAULT).toString();
        if (mStepGoal != "0") {
            progressRing.setProgress(stepCount * 100 / Integer.parseInt(mStepGoal));

            Log.d(TAG, " progres  百分比 " + stepCount * 100 / Integer.parseInt(mStepGoal));
            Log.d(TAG, " progres  mStepGoal " + mStepGoal);
            Log.d(TAG, " progres  stepcount " + stepCount);
            Log.d(TAG, " progres  符合");
        } else {
            Log.d(TAG, " progres  不符合");
            progressRing.setProgress(100);
        }
    }


    private void judgmentGoogleFit() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN )
    public void onEventReceive(Event event) {
        if (event.getCode()==EventBusUtil.EventCode.FLOATACTIONBUTTON_INIT){
            initProgress();
            initKmKcTime();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBusUtil.unregister(this);
        getActivity().unbindService(serviceConnection);
    }


    @Override
    public boolean allowShowAd() {
        return false;
    }
}
