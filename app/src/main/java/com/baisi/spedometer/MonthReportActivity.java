package com.baisi.spedometer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.baisi.spedometer.greendao.gen.StepDataDao;
import com.baisi.spedometer.step.bean.StepData;
import com.baisi.spedometer.step.utils.MyDbUtils;
import com.baisi.spedometer.step.utils.StepConversion;
import com.baisi.spedometer.view.chatview.SingleViewMonth;
import com.bestgo.adsplugin.ads.AdAppHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MonthReportActivity extends AppCompatActivity implements View.OnClickListener {
    private SingleViewMonth monthChartView;
    private TextView totalSteps;
    private TextView date;
    private TextView step_tv;
    private TextView calorle_tv;
    private TextView time_tv;
    private TextView distance_tv;
    private TextView average_steps;
    private static final String STEP_STR = "step", CALORIE_STR = "calorie", TIME_STR = "time", DISTANCE_STR = "distance";
    private String TAG = getClass().getSimpleName();
    //private static final int STEP_NUMBER = 0, CALORIE_NUMBER = 1, TIME_NUMBER = 2, DISTANCE_NUMBER = 3;
    private ArrayList<TextView> textViewsList = new ArrayList<>();
    private MyDbUtils myDbUtils;
    private FrameLayout  adFrameLayout ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_report);
        initView();
        initData();

           /*加载广告*/
        AdAppHelper.getInstance(getApplicationContext()).loadNewNative();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AdAppHelper.getInstance(getApplicationContext()).getNative(0, adFrameLayout, params);
    }


    private void initView() {
        monthChartView = findViewById(R.id.monthchartview);
        totalSteps = findViewById(R.id.total_steps);
        date = findViewById(R.id.date);
        step_tv = findViewById(R.id.step_tv);
        calorle_tv = findViewById(R.id.calorle_tv);
        time_tv = findViewById(R.id.time_tv);
        distance_tv = findViewById(R.id.distance_tv);
        totalSteps = findViewById(R.id.total_steps);
        average_steps = findViewById(R.id.average_steps);
        adFrameLayout =findViewById(R.id.ad_frame);

        step_tv.setOnClickListener(this);
        calorle_tv.setOnClickListener(this);
        time_tv.setOnClickListener(this);
        distance_tv.setOnClickListener(this);

        textViewsList.add(step_tv);
        textViewsList.add(calorle_tv);
        textViewsList.add(time_tv);
        textViewsList.add(distance_tv);

        date.setText(StepConversion.getTodaySimpleDate_MMM());
    }

    private void initData() {
        myDbUtils = new MyDbUtils(this);
        List<Float> list = getStepListOrStepsecondList(STEP_STR);
        monthChartView.setList(list, STEP_STR);
        performClickReportTv(false, STEP_STR);

    }


    /**
     * @param classification 0  代表是stepalist  1 代表是stepsecondList ;
     * @return
     */
    private List<Float> getStepListOrStepsecondList(String classification) {

        Log.d(TAG, "getStepListOrStepsecondList: " + StepConversion.getMonth_MM(new Date()));

        List<Float> stepList = new ArrayList();
        stepList.clear();
        ArrayList arrayListEmpty = new ArrayList();
        int number = StepConversion.getCurrentMonthDay();
        //        for (int i = 0; i <number; i++) {
        //            stepList.add(i,30f);
        //        }
        //stepList.addAll(arrayListEmpty);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 1; i <= number; i++) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, i);//设置为1号,当前日期既为本月第一天
            String dayOfMonth = format.format(c.getTime());

            List<StepData> stepTodayAllDataList = myDbUtils.getDaoManager().getDaoSession().getStepDataDao()
                    .queryBuilder()
                    .where(StepDataDao.Properties.StepToday.eq(dayOfMonth) , StepDataDao.Properties.Reset.eq("false"))
                    .list();

            int todayAllStep = 0;

            if (classification.equals(STEP_STR)) {
                for (int j = 0; j < stepTodayAllDataList.size(); j++) {
                    todayAllStep += stepTodayAllDataList.get(j).getStep();
                }
            } else {
                for (int j = 0; j < stepTodayAllDataList.size(); j++) {
                    todayAllStep += stepTodayAllDataList.get(j).getStepSecond();
                }
            }

            if (classification.equals(STEP_STR)){
                stepList.add(i - 1, (float) todayAllStep);
            }else {
                stepList.add(i - 1, ((float) todayAllStep)/60f);
            }
        }

        Log.d(TAG, "getStepListOrStepsecondList:   stepListL: " + stepList.toString() + " size   " + stepList.size()  +"   number >>>"+number);
        return stepList;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.step_tv:
                performClickReportTv(false, STEP_STR);
                break;
            case R.id.calorle_tv:
                performClickReportTv(false, CALORIE_STR);
                break;
            case R.id.time_tv:
                performClickReportTv(false, TIME_STR);
                break;
            case R.id.distance_tv:
                performClickReportTv(false, DISTANCE_STR);
                break;
        }
    }

    public void performClickReportTv(boolean isFromPedometer, String classification) {
        if (this == null) {
            return;
        }
        switch (classification) {
            case STEP_STR:
                setTopTotalAndAverage(STEP_STR);
                monthChartView.setChartPaintColor(getResources().getColor(R.color.stepcolor));
                setChartList(getStepListOrStepsecondList(STEP_STR), STEP_STR);
                break;
            case CALORIE_STR:
                setTopTotalAndAverage(CALORIE_STR);
                monthChartView.setChartPaintColor(getResources().getColor(R.color.caloriecolor));
                // mMySingleChartView.setList(singlelist);
                ArrayList calorlelist = StepConversion.getCalorieList(getStepListOrStepsecondList(STEP_STR), this);
                setChartList(calorlelist, CALORIE_STR);
                break;
            case TIME_STR:
                setTopTotalAndAverage(TIME_STR);
                monthChartView.setChartPaintColor(getResources().getColor(R.color.timecolor));
                //mMySingleChartView.setList(singlelist ,"time");
                List timeList = getStepListOrStepsecondList(TIME_STR);
                setChartList(timeList, TIME_STR);
                break;
            case DISTANCE_STR:
                setTopTotalAndAverage(DISTANCE_STR);
                monthChartView.setChartPaintColor(getResources().getColor(R.color.distancecolor));
                //mMySingleChartView.setList(singlelist ,"distance");
                ArrayList distancelist = StepConversion.getDistanceList(getStepListOrStepsecondList(STEP_STR), this);
                setChartList(distancelist, DISTANCE_STR);
                break;
        }
        setSeclectTextView(classification, isFromPedometer);

    }

    private void getTodayStepAll() {

    }

    private void setChartList(List list, String classifcation) {

        if (list != null) {
            monthChartView.setList(list, classifcation);
        } else {
            List list1 = new ArrayList();
            monthChartView.setList(list1, classifcation);
        }
    }


    /**
     * 设置toal 数据；
     *
     * @param classification
     */
    private void setTopTotalAndAverage(String classification) {
        List stepList = getStepListOrStepsecondList(STEP_STR);
        List secondList = getStepListOrStepsecondList(TIME_STR);
        List calorList = StepConversion.getCalorieList(stepList, this);
        List distanceList = StepConversion.getDistanceList(stepList, this);


        float totalStep = StepConversion.getTotalFloatSumList(stepList);
        float totalCalor = StepConversion.getTotalFloatSumList(calorList);
        float totalDistance = StepConversion.getTotalFloatSumList(distanceList);
        float totalTime = StepConversion.getTotalFloatSumList(secondList);

        String suffixString = null;
        String averageString = null;
        int  number = StepConversion.getCurrentMonthDay() ;
        switch (classification) {
            case STEP_STR:
                suffixString = String.format("%.0f", totalStep) + " Steps";
                averageString = String.format("%.0f", totalStep /number );
                break;
            case CALORIE_STR:
                suffixString = String.format("%.2f", totalCalor) + " Kcal";
                averageString = String.format("%.2f", totalCalor / number);
                break;
            case DISTANCE_STR:
                suffixString = String.format("%.2f", totalDistance) + " Km";
                averageString = String.format("%.2f", totalDistance / number);
                break;
            case TIME_STR:
                suffixString = String.format("%.2f", totalTime ) + " m";
                averageString = String.format("%.2f", totalTime / (number));
                break;
        }
        totalSteps.setText(suffixString);
        average_steps.setText(getString(R.string.daily_average) + averageString);

    }

    /**
     * textview 点击显示状态改变
     *
     * @param classification
     */
    private void setSeclectTextView(String classification, boolean isFromPedometer) {
        int classificationNumber = 0;
        if (classification.equals(STEP_STR)) {
            classificationNumber = 0;
        } else if (classification.equals(CALORIE_STR)) {
            classificationNumber = 1;
        } else if (classification.equals(TIME_STR)) {
            classificationNumber = 2;
        } else if (classification.equals(DISTANCE_STR)) {
            classificationNumber = 3;
        }
        for (int i = 0; i < textViewsList.size(); i++) {
            if (classificationNumber == i) {
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
}
