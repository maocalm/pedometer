package com.baisi.spedometer;

import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.baisi.spedometer.adapter.HistoryAdapter;
import com.baisi.spedometer.greendao.gen.StepDataDao;
import com.baisi.spedometer.step.bean.StepData;
import com.baisi.spedometer.step.bean.StepDataNormal;
import com.baisi.spedometer.step.utils.MyDbUtils;
import com.baisi.spedometer.step.utils.StepConversion;
import com.baisi.spedometer.view.CustomTopBar;
import com.bestgo.adsplugin.*;
import com.bestgo.adsplugin.ads.AdAppHelper;
import com.bestgo.adsplugin.ads.activity.ShowAdFilter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener, ShowAdFilter {

    private CustomTopBar customTopBar;
    private RecyclerView recyclerView;
    private FrameLayout frameLayout;
    private StepDataDao stepDataDao;
    private String TAG = getClass().getSimpleName();
    private RelativeLayout relativeLayout;
    private FrameLayout.LayoutParams params;
    private ContentLoadingProgressBar contentLoadingProgressBar;
    private RelativeLayout progress_rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initViewAndData();
        initRecycleView();
        showAd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdAppHelper.getInstance(getApplicationContext()).getNative(0, frameLayout, params);
    }

    private void initViewAndData() {
        //customTopBar = findViewById(R.id.top_bar);
        relativeLayout = findViewById(R.id.top);
        recyclerView = findViewById(R.id.historyrecycleview);
        frameLayout = findViewById(R.id.ad_frame);
        contentLoadingProgressBar = findViewById(R.id.contentload);
        progress_rl = findViewById(R.id.progress_rl);
        relativeLayout.setOnClickListener(this);
        MyDbUtils myDbUtils = new MyDbUtils(this);
        stepDataDao = myDbUtils.getDaoManager().getDaoSession().getStepDataDao();
    }

    private void initRecycleView() {
        sub();
        //List<StepDataNormal> stepDataNormalsList = getRecycleViewData();
//        HistoryAdapter historyAdapter = new HistoryAdapter(stepDataNormalsList, this);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setAdapter(historyAdapter);
    }

    private void showAd() {
        /*加载广告*/
        AdAppHelper.getInstance(getApplicationContext()).loadNewNative();
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }


    private List<StepDataNormal> getRecycleViewData() {

        List<StepDataNormal> recycleViewList = new ArrayList<>();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();

        //计算数据库中第一条和最后一条的时间差 （天）
        int count = stepDataDao.loadAll().size();

        if (count < 1) {  //数据库为空
            Log.d(TAG, "getRecycleViewData: " + "数据库为空的");
            return recycleViewList;
        }

        // 计算数据库中开始到现在总共多少天；
        String firstdata = "";
        String lastdata = "";
        List<StepData> list = stepDataDao.queryBuilder()
                .where(StepDataDao.Properties.Reset.eq("false"))
                .orderAsc(StepDataDao.Properties.StepToday).list();
        for (int i = 0; i < list.size(); i++) {
            Log.e(TAG, "getRecycleViewData: ----->" + list.get(i).getStepToday());
        }
        firstdata = list.get(0).getStepToday();
        lastdata = list.get(list.size() - 1).getStepToday();
        long firstdataLong = StepConversion.getSimpleDateFromString(firstdata).getTime();
        long lastdataLong = StepConversion.getSimpleDateFromString(lastdata).getTime();
        int days = (int) ((lastdataLong - firstdataLong + (1000 * 60 * 60 * 24)) / (1000 * 60 * 60 * 24));

        Date dateXunhuan = list.get(list.size() - 1).getStepTime();

        StepDataNormal stepDataNormalTitleFirst = getWeekStepDataNormal(dateXunhuan);
        //recycleViewList.add(stepDataNormalTitleFirst);

        for (int i = 0; i < days; i++) {

            //往前推一天
            calendar.setTime(dateXunhuan);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            Date dateBefor = calendar.getTime();

            // 2018/1/3 判断是否是一周中的最后有数据的一天  加空字段填充作为title ；
            List<Date> dateList = StepConversion.getDateWeekList(dateXunhuan);
            List<StepData> stepDataList = stepDataDao.queryBuilder()
                    .where(StepDataDao.Properties.StepTime
                            .between(dateList.get(0), dateList.get(dateList.size() - 1)), StepDataDao.Properties.Reset.eq("false"), StepDataDao.Properties.Step.notEq(0))
                    .orderAsc()
                    .list();
            if (stepDataList.size() != 0) {

                if (StepConversion.getSimpleDate(dateXunhuan).equals(stepDataList.get(stepDataList.size() - 1).getStepToday())) {
                    StepDataNormal stepDataNormalTitle = getWeekStepDataNormal(dateXunhuan);
                    recycleViewList.add(stepDataNormalTitle);
                }

            /*boolean isSameWeek = StepConversion.isSameWeekWithToday(date, dateBefor);
            if (!isSameWeek) {
                StepDataNormal stepDataNormalTitle = getWeekStepDataNormal(date);
                recycleViewList.add(stepDataNormalTitle);
            }*/

                //填充content
                StepDataNormal stepDataNormal = getNewStepData(dateXunhuan);
                recycleViewList.add(stepDataNormal);
            }

            dateXunhuan = dateBefor;
        }
        Log.d(TAG, "getRecycleViewData: list:" + recycleViewList.toString());
        return recycleViewList;
    }

    /**
     * 获取传入的date的总步数，second,构造stepDataNormal ;
     *
     * @param date
     * @return
     */
    private StepDataNormal getNewStepData(Date date) {
        List<StepData> stepDataList = stepDataDao.queryBuilder()
                .where(StepDataDao.Properties.StepToday.eq(StepConversion.getSimpleDate(date)), StepDataDao.Properties.Reset.eq("false")).list();
        StepDataNormal stepDataNormal = new StepDataNormal();
        int step = 0;
        int stepSecond = 0;
        for (int i = 0; i < stepDataList.size(); i++) {
            step += stepDataList.get(i).getStep();
            stepSecond += stepDataList.get(i).getStepSecond();
        }

        stepDataNormal.setStep(step);
        stepDataNormal.setStepTime(date);
        stepDataNormal.setStepSecond(stepSecond);
        stepDataNormal.setStepToday(StepConversion.getSimpleDate(date));
        stepDataNormal.setIsTopTitle(false);
        Log.d(TAG, "getNewStepData: " + stepDataNormal);
        return stepDataNormal;
    }

    /**
     * 获取传入date本周的stepdata ，构造stepdatanormal  作为recycleView 的 title  ;
     *
     * @param date
     * @return StepdataNormal
     */
    private StepDataNormal getWeekStepDataNormal(Date date) {
        StepDataNormal stepDataNormal = new StepDataNormal();
        List<Date> dateList = StepConversion.getDateWeekList(date);
        int step = 0;
        int stepSecond = 0;
        List<StepData> stepDataList = stepDataDao.queryBuilder()
                .where(StepDataDao.Properties.StepTime.between(dateList.get(0), dateList.get(dateList.size() - 1)), StepDataDao.Properties.Reset.eq("false"))
                .orderAsc()
                .list();
        for (int i = 0; i < stepDataList.size(); i++) {
            step += stepDataList.get(i).getStep();
            stepSecond += stepDataList.get(i).getStepSecond();
        }
        stepDataNormal.setIsTopTitle(true);
        stepDataNormal.setStepSecond(stepSecond);
        stepDataNormal.setStep(step);
        stepDataNormal.setStepTime(date);
        stepDataNormal.setStepToday(StepConversion.getSimpleDate(date));
        return stepDataNormal;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.top:
                finish();
                break;
        }
    }

    @Override
    public boolean allowShowAd() {
        return false;
    }

    Observer<List<StepDataNormal>> observer = new Observer<List<StepDataNormal>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(List<StepDataNormal> value) {

            HistoryAdapter historyAdapter = new HistoryAdapter(value, HistoryActivity.this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(HistoryActivity.this, RecyclerView.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(historyAdapter);
            showProgressBar(false);
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    };

    private void sub() {

        Observable.create(new ObservableOnSubscribe<List<StepDataNormal>>() {
            @Override
            public void subscribe(ObservableEmitter<List<StepDataNormal>> e) throws Exception {
                showProgressBar(true);
                List<StepDataNormal> list = getRecycleViewData();
                e.onNext(list);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    private void showProgressBar(boolean show) {
        if (show == true) {
            contentLoadingProgressBar.show();
            progress_rl.setVisibility(View.VISIBLE);
        } else {
            contentLoadingProgressBar.hide();
            progress_rl.setVisibility(View.GONE);
        }
    }
}
