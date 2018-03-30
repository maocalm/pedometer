package com.baisi.spedometer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baisi.spedometer.evenbus.Event;
import com.baisi.spedometer.evenbus.EventBusUtil;
import com.baisi.spedometer.greendao.gen.StepDataDao;
import com.baisi.spedometer.step.bean.StepData;
import com.baisi.spedometer.step.utils.MyDbUtils;
import com.baisi.spedometer.step.utils.StepConversion;
import com.baisi.spedometer.view.PercentageBar;
import com.baisi.spedometer.view.chatview.SingView2;
import com.bestgo.adsplugin.ads.AdAppHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class WeekReportActivity extends AppCompatActivity implements View.OnClickListener {
    private View view;
    private PercentageBar columnChartView;
    private static SingView2 mMySingleChartView;
    private List<Float> singlelist;
    private RelativeLayout rlSingle;
    private LinearLayout llSingle;
    private TextView step_tv;
    private TextView calorle_tv;
    private TextView time_tv;
    private TextView distance_tv;
    private static final int STEP_NUMBER = 0, CALORIE_NUMBER = 1, TIME_NUMBER = 2, DISTANCE_NUMBER = 3;
    private static final String STEP_STR = "step", CALORIE_STR = "calorie", TIME_STR = "time", DISTANCE_STR = "distance";
    private ArrayList<TextView> textViewsList = new ArrayList<>();
    private TextView total_steps;
    private TextView average_steps;
    private TextView datas_steps;
    private String TAG = "reportfragment";
    private MyDbUtils myDbUtils;
    private int classicationInt;
    private static int classicationInt1;
    private RelativeLayout frame_week_rel;
    private FrameLayout ad_frame_weekreport;
    private List<Float> stepList;
    private List<Float> secondList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_report);
        EventBusUtil.register(this);
        initView();
        onNewIntent(getIntent());
    }

    private void initView() {

        total_steps = (TextView) findViewById(R.id.total_steps);
        average_steps = (TextView) findViewById(R.id.average_steps);
        datas_steps = (TextView) findViewById(R.id.datas_steps);

        step_tv = (TextView) findViewById(R.id.step_tv);
        calorle_tv = (TextView) findViewById(R.id.calorle_tv);
        time_tv = (TextView) findViewById(R.id.time_tv);
        distance_tv = (TextView) findViewById(R.id.distance_tv);
        ad_frame_weekreport = findViewById(R.id.ad_frame);

        textViewsList.add(step_tv);
        textViewsList.add(calorle_tv);
        textViewsList.add(time_tv);
        textViewsList.add(distance_tv);
        mMySingleChartView = (SingView2) findViewById(R.id.chartview);
        rlSingle = (RelativeLayout) findViewById(R.id.rl_single);

        step_tv.setOnClickListener(this);
        calorle_tv.setOnClickListener(this);
        time_tv.setOnClickListener(this);
        distance_tv.setOnClickListener(this);
        step_tv.setSelected(true);
        myDbUtils = new MyDbUtils(this);


        /*加载广告*/
        AdAppHelper.getInstance(getApplicationContext()).loadNewNative();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AdAppHelper.getInstance(getApplicationContext()).getNative(0, ad_frame_weekreport, params);


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.hasExtra("TYPE")) {
            String type = intent.getStringExtra("TYPE");
            intent.removeExtra("TYPE");
            if (type.equals("kcal")) {
                initSingle(CALORIE_NUMBER);
            } else if (type.equals("distance")) {
                initSingle(DISTANCE_NUMBER);
            } else if (type.equals("time")) {
                initSingle(TIME_NUMBER);
            }
        } else {
            initSingle(STEP_NUMBER);
        }

    }

    /**
     * 设置toal 数据；
     *
     * @param classification
     */
    private void setTopTotalAndAverage(int classification) {
        stepList = getStepList(STEP_STR);
        secondList = getStepList(TIME_STR);
        List calorList = StepConversion.getCalorieList(stepList, this);
        List distanceList = StepConversion.getDistanceList(stepList, this);

        float totalStep = StepConversion.getTotalFloatSumList(stepList);
        float totalCalor = StepConversion.getTotalFloatSumList(calorList);
        float totalDistance = StepConversion.getTotalFloatSumList(distanceList);
        float totalTime = StepConversion.getTotalFloatSumList(secondList);

        String suffixString = null;
        String averageString = null;
        switch (classification) {
            case STEP_NUMBER:
                suffixString = String.format("%.0f", totalStep) + " Steps";
                averageString = String.format("%.0f", totalStep / 7);
                break;
            case CALORIE_NUMBER:
                suffixString = String.format("%.2f", totalCalor) + " Kcal";
                averageString = String.format("%.2f", totalCalor / 7);
                break;
            case DISTANCE_NUMBER:
                suffixString = String.format("%.2f", totalDistance) + " Km";
                averageString = String.format("%.2f", totalDistance / 7);
                break;
            case TIME_NUMBER:
                suffixString = String.format("%.2f", totalTime) + " m";
                averageString = String.format("%.2f", totalTime / (7));
                break;
        }
        total_steps.setText(suffixString);
        average_steps.setText(getString(R.string.daily_average) + averageString);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.step_tv:
                performClickReportTv(false, STEP_NUMBER);
                mMySingleChartView.setChartPaintColor(getResources().getColor(R.color.stepcolor));
                break;
            case R.id.calorle_tv:
                performClickReportTv(false, CALORIE_NUMBER);
                mMySingleChartView.setChartPaintColor(getResources().getColor(R.color.caloriecolor));
                break;
            case R.id.time_tv:
                performClickReportTv(false, TIME_NUMBER);
                mMySingleChartView.setChartPaintColor(getResources().getColor(R.color.timecolor));
                break;
            case R.id.distance_tv:
                performClickReportTv(false, DISTANCE_NUMBER);
                mMySingleChartView.setChartPaintColor(getResources().getColor(R.color.distancecolor));
                break;
        }

    }


    /**
     * textview 点击显示状态改变
     *
     * @param textViewNumber
     */

    private void setSeclectTextView(int textViewNumber, boolean isFromPedometer) {
        for (int i = 0; i < textViewsList.size(); i++) {
            if (textViewNumber == i) {
                if (isFromPedometer) {
                    textViewsList.get(i).setSelected(true);
                } else {
                    //textViewsList.get(i).setSelected(!textViewsList.get(i).isSelected());
                    textViewsList.get(i).setSelected(true);
                }
            } else {
                textViewsList.get(i).setSelected(false);

            }
        }
    }

    public void performClickReportTv(boolean isFromPedometer, int number) {
        if (this == null) {
            return;
        }
        switch (number) {
            case STEP_NUMBER:
                setTopTotalAndAverage(STEP_NUMBER);
                mMySingleChartView.setChartPaintColor(getResources().getColor(R.color.stepcolor));
                //getStepListOrStepsecondList(0 ,STEP_STR);
                setChartList(stepList, STEP_STR);
                break;
            case CALORIE_NUMBER:
                setTopTotalAndAverage(CALORIE_NUMBER);
                mMySingleChartView.setChartPaintColor(getResources().getColor(R.color.caloriecolor));
                // mMySingleChartView.setList(singlelist);
                //getStepListOrStepsecondList(0,CALORIE_STR);
                setChartList(StepConversion.getCalorieList(stepList, this), CALORIE_STR);
                break;
            case TIME_NUMBER:
                setTopTotalAndAverage(TIME_NUMBER);
                mMySingleChartView.setChartPaintColor(getResources().getColor(R.color.timecolor));
                //mMySingleChartView.setList(singlelist ,"time");
                setChartList(secondList, TIME_STR);
                break;
            case DISTANCE_NUMBER:
                setTopTotalAndAverage(DISTANCE_NUMBER);
                mMySingleChartView.setChartPaintColor(getResources().getColor(R.color.distancecolor));
                //mMySingleChartView.setList(singlelist ,"distance");
                //getStepListOrStepsecondList(0, DISTANCE_STR);
                setChartList(StepConversion.getDistanceList(stepList, this), DISTANCE_STR);
                break;
        }
        setSeclectTextView(number, isFromPedometer);

    }


    private long getZoreOrTwelve(Date date, int zore_twelve) {

        long current = date.getTime();
        long zero = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
        long twelve = zero + 24 * 60 * 60 * 1000 - 1;//今天23点59分59秒的毫秒数

        if (zore_twelve == 0) {
            return zero;
        } else {
            return twelve;
        }
    }


    private List<Float> getStepList(String classification) {
        List<Date> dateArrayList = StepConversion.getDateWeekList(new Date());
        List<Float> stepList = new ArrayList();
        for (int i = 0; i < dateArrayList.size(); i++) {
            String todayString = StepConversion.getSimpleDate(dateArrayList.get(i));
            List<StepData> resultQuer = myDbUtils.getDaoManager()
                    .getDaoSession()
                    .getStepDataDao()
                    .queryBuilder()
                    .where(StepDataDao.Properties.StepToday.eq(todayString), StepDataDao.Properties.Reset.eq("false"))
                    .list();
            Log.d(TAG, "一天内的 数据集" + resultQuer.toString());

            if (resultQuer != null && resultQuer.size() != 0) {
                float stepcount = 0;
                float stepSecondCount = 0;
                for (int j = 0; j < resultQuer.size(); j++) {
                    if (classification.equals(STEP_STR)) {
                        stepcount += resultQuer.get(j).getStep();
                    } else {
                        stepSecondCount += resultQuer.get(j).getStepSecond();
                    }
                }

                if (classification.equals(STEP_STR)) {
                    stepList.add(stepcount);
                } else {
                    stepList.add(stepSecondCount);
                }
            } else {
                if (classification.equals(STEP_STR)) {
                    stepList.add(0f);
                } else {
                    stepList.add(0f);
                }
            }
        }
        return stepList;
    }


    private void setChartList(List<Float> list, String classifcation) {

        if (list != null) {
            mMySingleChartView.setList(list, classifcation);
        } else {
            List list1 = new ArrayList();
            list1.add(0f);
            list1.add(0f);
            list1.add(0f);
            list1.add(0f);
            list1.add(0f);
            list1.add(0f);
            list1.add(0f);
            mMySingleChartView.setList(list1, classifcation);
        }
    }

    /**
     * 初始化单柱柱状图
     */
    private void initSingle(int type) {
        classicationInt1 = 0;
        stepList = new ArrayList();
        Log.d(TAG, "  initsingle  methode ");
        getStepList(STEP_STR);
        setChartList(getStepList(STEP_STR), STEP_STR);
        performClickReportTv(false, type);

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventReceive(Event event) {
        switch (event.getCode()) {
            case EventBusUtil.EventCode.GOWEEKACTIVITY:
                switch (event.getData().toString()) {
                    case "time":
                        break;
                    case "kcal":
                        performClickReportTv(true, CALORIE_NUMBER);
                        Log.e(TAG, "onEventReceive: ------->" + CALORIE_NUMBER);
                        break;
                    case "distance":
                        break;
                }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtil.unregister(this);
    }
}
