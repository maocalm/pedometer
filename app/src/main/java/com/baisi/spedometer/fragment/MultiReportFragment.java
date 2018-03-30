package com.baisi.spedometer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.baisi.spedometer.MonthReportActivity;
import com.baisi.spedometer.R;
import com.baisi.spedometer.TodayReportActivity;
import com.baisi.spedometer.WeekReportActivity;
import com.baisi.spedometer.base.BaseFragment;
import com.baisi.spedometer.greendao.gen.StepDataDao;
import com.baisi.spedometer.step.bean.StepData;
import com.baisi.spedometer.step.utils.MyDbUtils;
import com.baisi.spedometer.step.utils.StepConversion;
import com.baisi.spedometer.view.chatview.SingViewToday;
import com.baisi.spedometer.view.chatview.SingleViewMonth;
import com.baisi.spedometer.view.chatview.SingleViewWeekMulti;
import com.bestgo.adsplugin.ads.AdAppHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by hanwenmao on 2017/12/25.
 */

public class MultiReportFragment extends BaseFragment {

    private SingleViewWeekMulti weekchartView;
    private SingViewToday todaychartView;
    private SingleViewMonth monthchartView;
    private String TAG = getClass().getSimpleName();
    private static final String STEP_STR = "step", CALORIE_STR = "calorie", TIME_STR = "time", DISTANCE_STR = "distance";
    private TextView step_tv;
    private MyDbUtils dbUtils;

    private List<Float> stepList;
    private List<Float> stepSecondList;
    private MyDbUtils myDbUtils;

    private FrameLayout ad_frame1;

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.multi_report_fragment, null);

        weekchartView = view.findViewById(R.id.weekchartview);
        todaychartView = view.findViewById(R.id.todaychartview);
        monthchartView = view.findViewById(R.id.monthchartview);
        ad_frame1 = view.findViewById(R.id.ad_frame1);
        dbUtils = new MyDbUtils(getActivity());


        step_tv = view.findViewById(R.id.step_tv);
        TextView calor_tv = view.findViewById(R.id.calorle_tv);
        TextView time_tv = view.findViewById(R.id.time_tv);
        TextView distance_tv = view.findViewById(R.id.distance_tv);

        stepList = new ArrayList<>();
        stepSecondList = new ArrayList<>();
        myDbUtils = new MyDbUtils(getActivity());
        initSingView();
        view.findViewById(R.id.today_relayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TodayReportActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.week_relayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              /*  FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ReportFragment reportFragment = new ReportFragment();
                frame_week_rel.setVisibility(View.VISIBLE);
                fragmentTransaction.replace(R.id.framelayout_week, reportFragment);
                fragmentTransaction.commit();*/

                Intent intent = new Intent(getActivity(), WeekReportActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.month_relayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MonthReportActivity.class);
                startActivity(intent);
            }
        });

        AdAppHelper.getInstance(getApplicationContext()).loadNewNative(1);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AdAppHelper.getInstance(getApplicationContext()).getNative(1 , ad_frame1, params);


        return view;
    }


    private List<Float> getStepList(String classification) {
        List<Date> dateArrayList = StepConversion.getDateWeekList(new Date());
        //Log.d(TAG, " 今天的步骤列表是多少 ；    。。。。 数据库" + resultQuer.toString());
        stepList.clear();
        stepSecondList.clear();
        for (int i = 0; i < dateArrayList.size(); i++) {
            String todayString = StepConversion.getSimpleDate(dateArrayList.get(i));
            List<StepData> resultQuer = dbUtils.getDaoManager().getDaoSession().getStepDataDao().queryBuilder().where(StepDataDao.Properties.StepToday.eq(todayString)).list();
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
                    stepSecondList.add(stepSecondCount);
                }

            } else {
                if (classification.equals(STEP_STR)) {
                    stepList.add(0f);
                } else {
                    stepSecondList.add(0f);
                }
            }
        }
        Log.d(TAG, "得到的周步骤列表" + stepList.toString());
        return (classification.equals(STEP_STR)) ? stepList : stepSecondList;
    }

    @Override
    protected void initData() {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        //super.setUserVisibleHint(isVisibleToUser);

        Log.d(TAG, "setUserVisibleHint: ");
        if (isVisibleToUser){
            /*加载广告*/
            AdAppHelper.getInstance(getApplicationContext()).loadNewNative(1);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            AdAppHelper.getInstance(getApplicationContext()).getNative(1 , ad_frame1, params);
            AdAppHelper.getInstance(getApplicationContext()).showFullAd();
        }
    }

    @Override
    protected void setDefaultFragmentTitle(String title) {

    }

    private void initSingView() {
        step_tv.setSelected(true);
        getStepList(STEP_STR);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                weekchartView.setList(stepList, STEP_STR);
                todaychartView.setList(getStepTodayList(), STEP_STR);
                monthchartView.setList(getStepMonthList(), STEP_STR);
            }
        }, 200);


    }

    /**
     * @param classification 0  代表是stepalist  1 代表是stepsecondList ;
     * @return
     */
    private List getStepListOrStepsecondList(int classification) {
        List<StepData> stepAllDataList = dbUtils.getDaoManager().getDaoSession().getStepDataDao().loadAll();

        List stepList = new ArrayList();
        List stepSecondList = new ArrayList();
        int location;

        StepData firstDate = dbUtils.queryStepDataById(1);
        if (dbUtils.queryAllStepData().size() == 0 || firstDate == null) {
            for (int i = 0; i < 7; i++) {
                stepList.add(0f);
            }
            return stepList;
        }
        //try {
        Log.d(TAG, "======firstdateString =====" + firstDate.toString());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //Date firstDate = dateFormat.parse(firstDateString);
        Log.d(TAG, "date =====" + dateFormat.format(firstDate.getStepTime()));
        location = StepConversion.getDayofweek(firstDate.getStepTime());
        Log.d(TAG, "dayofweek " + location);

        for (int i = 1; i < location; i++) {

            stepList.add(0f);
            stepSecondList.add(0f);
        }


        for (int i = 0; i < stepAllDataList.size(); i++) {
            try {
                Float step = Float.valueOf(stepAllDataList.get(i).getStep());
                stepList.add(step);
                Float stepSecond = (float) stepAllDataList.get(i).getStepSecond();
                stepSecondList.add(stepSecond / 60f);
            } catch (Exception e) {

            }

        }

        Log.d(TAG, "===steplist===" + stepList.toString());
        Log.d(TAG, "===stepsecondList===" + stepSecondList.toString());
        Log.d(TAG, "==========" + stepAllDataList.toString());
        if (classification == 0) {
            Log.d(TAG, " report 设置的steplist  " + stepList.toString());
            return stepList;
        } else {
            Log.d(TAG, " report 设置的stepsecond list  " + stepList.toString());
            return stepSecondList;
        }
    }

    private List<Float> getStepTodayList() {
        List<StepData> stepTodayAllDataList = myDbUtils.getDaoManager().getDaoSession().getStepDataDao()
                .queryBuilder()
                .where(StepDataDao.Properties.StepToday.eq(StepConversion.getTodaySimpleDate()), StepDataDao.Properties.Reset.eq("false"))
                .list();

        List<Float> stepList = new ArrayList();

        ArrayList arrayListEmpty = new ArrayList();
        for (int i = 0; i < 24; i++) {
            arrayListEmpty.add(0f);
        }
        stepList.addAll(arrayListEmpty);

        if (stepTodayAllDataList != null) {
            for (int i = 0; i < stepTodayAllDataList.size(); i++) {
                int timehour = StepConversion.getStepHour(stepTodayAllDataList.get(i).getStepTime());
                Log.d(TAG, "index  a " + timehour);
                stepList.add(timehour, Float.valueOf(stepTodayAllDataList.get(i).getStep()));
            }
        }

        return stepList;

    }

    private List<Float> getStepMonthList() {
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
                    .where(StepDataDao.Properties.StepToday.eq(dayOfMonth), StepDataDao.Properties.Reset.eq("false"))
                    .list();

            int todayAllStep = 0;


            for (int j = 0; j < stepTodayAllDataList.size(); j++) {
                todayAllStep += stepTodayAllDataList.get(j).getStep();
            }

            stepList.add(i - 1, (float) todayAllStep);
        }

        Log.d(TAG, "getStepListOrStepsecondList:   stepListL: " + stepList.toString() + " size   " + stepList.size() + "   number >>>" + number);
        return stepList;
    }
}
