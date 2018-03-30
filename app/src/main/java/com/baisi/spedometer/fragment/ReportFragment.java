package com.baisi.spedometer.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baisi.spedometer.R;
import com.baisi.spedometer.base.BaseFragment;
import com.baisi.spedometer.greendao.gen.StepDataDao;
import com.baisi.spedometer.step.bean.StepData;
import com.baisi.spedometer.step.utils.MyDbUtils;
import com.baisi.spedometer.step.utils.StepConversion;
import com.baisi.spedometer.view.PercentageBar;
import com.baisi.spedometer.view.chatview.SingView2;
import com.baisi.spedometer.view.chatview.SingleView;
import com.bestgo.adsplugin.ads.AdAppHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by hanwenmao on 2017/11/21.
 */

public class ReportFragment extends BaseFragment implements View.OnClickListener {

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
    private List stepList;
    private List stepSecondList;
    private RelativeLayout frame_week_rel ;
    private FrameLayout  ad_frame_weekreport;
    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.report_fragment, null);
        total_steps = (TextView) view.findViewById(R.id.total_steps);
        average_steps = (TextView) view.findViewById(R.id.average_steps);
        datas_steps = (TextView) view.findViewById(R.id.datas_steps);

        step_tv = (TextView) view.findViewById(R.id.step_tv);
        calorle_tv = (TextView) view.findViewById(R.id.calorle_tv);
        time_tv = (TextView) view.findViewById(R.id.time_tv);
        distance_tv = (TextView) view.findViewById(R.id.distance_tv);

        step_tv.setTag(1);
        calorle_tv.setTag(2);
        time_tv.setTag(3);
        distance_tv.setTag(4);
        textViewsList.add(step_tv);
        textViewsList.add(calorle_tv);
        textViewsList.add(time_tv);
        textViewsList.add(distance_tv);


        step_tv.setOnClickListener(this);
        calorle_tv.setOnClickListener(this);
        time_tv.setOnClickListener(this);
        distance_tv.setOnClickListener(this);
        //columnChartView = (PercentageBar) view.findViewById(R.id.chartview);
        // initColume();
        step_tv.setSelected(true);
        myDbUtils = new MyDbUtils(getActivity());
        initSingle();

        /*加载广告*/
        AdAppHelper.getInstance(getApplicationContext()).loadNewNative();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AdAppHelper.getInstance(getApplicationContext()).getNative(0, ad_frame_weekreport, params);

        return view;
    }

    @Override
    protected void initData() {
//        columnChartView.
        Log.d(TAG, "reportfragment   initdata  ");

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (view != null) {
            Log.d(TAG, "onvisible ");
            getStepListOrStepsecondList( STEP_STR);
        }
    }


    /**
     * 设置toal 数据；
     *
     * @param classification
     */
    private void setTopTotalAndAverage(int classification) {
        // List stepList = getStepListOrStepsecondList(0 ,STEP_STR);
        // List secondList = getStepListOrStepsecondList(1 ,TIME_STR);
        List calorList = StepConversion.getCalorieList(stepList, getActivity());
        List distanceList = StepConversion.getDistanceList(stepList, getActivity());

        float totalStep = StepConversion.getTotalFloatSumList(stepList);
        float totalCalor = StepConversion.getTotalFloatSumList(calorList);
        float totalDistance = StepConversion.getTotalFloatSumList(distanceList);
        float totalTime = StepConversion.getTotalFloatSumList(stepSecondList);

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
        if (getActivity() == null) {
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
                setChartList(StepConversion.getCalorieList(stepList, getActivity()), CALORIE_STR);
                break;
            case TIME_NUMBER:
                setTopTotalAndAverage(TIME_NUMBER);
                mMySingleChartView.setChartPaintColor(getResources().getColor(R.color.timecolor));
                //mMySingleChartView.setList(singlelist ,"time");
                setChartList(stepSecondList, TIME_STR);
                break;
            case DISTANCE_NUMBER:
                setTopTotalAndAverage(DISTANCE_NUMBER);
                mMySingleChartView.setChartPaintColor(getResources().getColor(R.color.distancecolor));
                //mMySingleChartView.setList(singlelist ,"distance");
                //getStepListOrStepsecondList(0, DISTANCE_STR);
                setChartList(StepConversion.getDistanceList(stepList, getActivity()), DISTANCE_STR);
                break;
        }
        setSeclectTextView(number, isFromPedometer);

    }


    private void initColume() {
        ArrayList<String> respName;
        ArrayList respectTarget = new ArrayList<Float>();
        respName = new ArrayList<String>();
        respectTarget.add(35.0f);
        respectTarget.add(20.0f);
        respectTarget.add(18.0f);
        respectTarget.add(15.0f);
        respectTarget.add(10.0f);
        respectTarget.add(8.0f);
        respectTarget.add(5.0f);
        respName.add("滴滴");
        respName.add("小米");
        respName.add("京东");
        respName.add("美团");
        respName.add("魅族");
        respName.add("酷派");
        respName.add("携程");
        columnChartView.setRespectTargetNum(respectTarget);
        columnChartView.setRespectName(respName);
        columnChartView.setTotalBarNum(7);
        columnChartView.setMax(40);
        columnChartView.setBarWidth(50);
        columnChartView.setVerticalLineNum(10);
        columnChartView.setUnit("");
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


    Handler handler = new Handler();

    /**
     * @param classification 0  代表是stepalist  1 代表是stepsecondList ;
     * @return
     */
    private void getStepListOrStepsecondList(final String classification) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                getStepList(classification);
            }
        }).start();

    }

    private List<Float> getStepList(String classification) {
        List<Date> dateArrayList = StepConversion.getDateWeekList(new Date());
        //Log.d(TAG, " 今天的步骤列表是多少 ；    。。。。 数据库" + resultQuer.toString());
        stepList.clear();
        stepSecondList.clear();
        for (int i = 0; i < dateArrayList.size(); i++) {
            String todayString = StepConversion.getSimpleDate(dateArrayList.get(i));
            List<StepData> resultQuer = myDbUtils.getDaoManager()
                    .getDaoSession()
                    .getStepDataDao()
                    .queryBuilder()
                    .where(StepDataDao.Properties.StepToday.eq(todayString) , StepDataDao.Properties.Reset.eq("false"))
                    .list();
            Log.d(TAG, "一天内的 数据集" + resultQuer.toString());

            if (resultQuer != null && resultQuer.size() != 0) {
                float stepcount = 0;
                float stepSecondCount = 0;
                for (int j = 0; j < resultQuer.size(); j++) {
                    if (classification.equals(STEP_STR) ) {
                        stepcount += resultQuer.get(j).getStep();
                    } else {
                        stepSecondCount += resultQuer.get(j).getStepSecond();
                    }
                }

                if (classification.equals(STEP_STR)){
                    stepList.add(stepcount);
                }else {
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
        return (classification.equals(STEP_STR)) ? stepList : stepSecondList;
    }


    private String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    private void setChartList(List list, String classifcation) {

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
    private void initSingle() {
        classicationInt1 = 0;
        stepList = new ArrayList();
        stepSecondList = new ArrayList();
        Log.d(TAG, "  initsingle  methode ");
        mMySingleChartView = (SingView2) view.findViewById(R.id.chartview);
        rlSingle = (RelativeLayout) view.findViewById(R.id.rl_single);
        getStepListOrStepsecondList(STEP_STR);
        getStepListOrStepsecondList(TIME_STR);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setChartList(stepList, STEP_STR);
                setTopTotalAndAverage(0);
//                float total = StepConversion.getTotalFloatSumList(arrayListStep);
//                total_steps.setText(String.format("%.0f", total) + " " + getString(R.string.steps));
//                average_steps.setText(getString(R.string.daily_average) + " " + String.format("%.0f", total / 7));
//
                datas_steps.setText(StepConversion.getTodayWeekRange(new Date()));
            }
        }, 500);

        /**
         * 添加布局 stepText
         */
        mMySingleChartView.setListener(new SingleView.getNumberListener() {

            @Override
            public void getNumber(int number, int x, int y) {
                rlSingle.removeView(llSingle);
                llSingle = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.layout_pro_expense, null);
                TextView tvMoney = (TextView) llSingle.findViewById(R.id.tv_shouru_pro);
                tvMoney.setText((number + 1) + " " + (singlelist.get(number)));
                llSingle.measure(0, 0);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.leftMargin = x - 100;
                if (x - 100 < 0) {
                    params.leftMargin = 0;
                } else if (x - 100 > rlSingle.getWidth() - llSingle.getMeasuredWidth()) {
                    params.leftMargin = rlSingle.getWidth() - llSingle.getMeasuredWidth();
                }

                llSingle.setLayoutParams(params);
                //rlSingle.addView(llSingle); //
            }
        });
    }

    @Override
    protected void setDefaultFragmentTitle(String title) {

    }

    @Override
    public boolean interceptBackPressed() {
        if (true) {
            //Toast.makeText(getActivity(), "lllllll", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy:   reportfragment :" );
        frame_week_rel.setVisibility(View.GONE);

    }
}
