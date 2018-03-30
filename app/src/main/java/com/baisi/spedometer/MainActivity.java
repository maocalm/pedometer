package com.baisi.spedometer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baisi.spedometer.adapter.ViewPagerAdapter;
import com.baisi.spedometer.backfragment.BackHandlerHelper;
import com.baisi.spedometer.evenbus.Event;
import com.baisi.spedometer.evenbus.EventBusUtil;
import com.baisi.spedometer.fragment.MultiReportFragment;
import com.baisi.spedometer.fragment.PedometerFragment;
import com.baisi.spedometer.fragment.ReportFragment;
import com.baisi.spedometer.fragment.SettingFragment;
import com.baisi.spedometer.fragment.fragmentlistenner.PedometerFragmentListener;
import com.baisi.spedometer.greendao.gen.DaoSession;
import com.baisi.spedometer.greendao.gen.StepDataDao;
import com.baisi.spedometer.step.bean.StepData;
import com.baisi.spedometer.step.utils.MyDbUtils;
import com.baisi.spedometer.step.utils.SharedPreferencesUtils;
import com.baisi.spedometer.step.utils.StepConversion;
import com.baisi.spedometer.utiles.Firebase;
import com.baisi.spedometer.utiles.OSUtils;
import com.baisi.spedometer.view.CustomPopWindow;
import com.baisi.spedometer.view.floataction.FloatingActionButton;
import com.baisi.spedometer.view.floataction.FloatingActionMenu;
import com.bestgo.adsplugin.ads.AdAppHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private TabLayout mTablayout;
    private ViewPager mViepager;
    private SharedPreferencesUtils sp;
    private PedometerFragment pedometerFragment;
    private ReportFragment reportFragment;
    private static final int STEP_NUMBER = 0, CALORIE_NUMBER = 1, TIME_NUMBER = 2, DISTANCE_NUMBER = 3;

    private FrameLayout adFrameLayout;
    private long lastTime;
    private String TAG = "mainactivity";

    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    private View view_mainView;
    private CustomPopWindow popWindow;
    private ContentLoadingProgressBar contentLoadingProgressBar;
    private View popProgressBarView;
    private TextView pop_textViewSubTitle;
    private View pop_view;
    private FrameLayout.LayoutParams params;


    private FloatingActionMenu menuDown;
    private MyDbUtils myDbUtils;
    private FloatingActionButton timeline_float;
    private FloatingActionButton reset_float;

    private DaoSession daoSession;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(0x80000000, 0x80000000);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "mainactivity   ====?>>>>>>");
        adFrameLayout = findViewById(R.id.ad_frame);
        /*加载广告*/
        AdAppHelper.getInstance(getApplicationContext()).loadNewNative();
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AdAppHelper.getInstance(getApplicationContext()).showFullAd();

        /*FireBase统计*/
        Firebase.getInstance(getApplicationContext()).logEvent("主页", " 进入 MainActivity");
        initDate();
        initView();

        /*开启计步服务*/
        //startStepService();
        setViewPager();

        //申请谷歌权限，读取数据在servic 线程中读取；
        judGmentGoogleFit();

        floatSetting();
    }

    public Context getContext() {
        return this;
    }
    private void floatSetting() {
        menuDown.setClosedOnTouchOutside(true);
//        menuDown.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                menuDown.close(true);
//                Log.d(TAG, "onTouch: clostfloatbutton ");
//                return true;
//            }
//        });

        menuDown.setOnMenuButtonClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:   setOnMenuButtonClickListener");
                menuDown.toggle(true);
            }
        });

        timeline_float.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
        reset_float.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<StepData> stepDataList = daoSession.getStepDataDao()
                        .queryBuilder()
                        .where(StepDataDao.Properties.StepToday.eq(StepConversion.getTodaySimpleDate()))
                        .list();

                //删除当天的数据
                for (int i = 0; i < stepDataList.size(); i++) {
                    StepData stepData = stepDataList.get(i);
                    daoSession.getStepDataDao().delete(stepData);
                }

                List<StepData> list = daoSession.getStepDataDao().loadAll();
                for (int i = 0; i < list.size(); i++) {
                    StepData stepData = list.get(i);
                    stepData.setReset("true");
                    stepData.setId(list.get(i).getId());
                    daoSession.getStepDataDao().update(stepData);
                }
                EventBusUtil.sendEvent(new Event(EventBusUtil.EventCode.RESTSTEPDATA));
                EventBusUtil.sendEvent(new Event(EventBusUtil.EventCode.FLOATACTIONBUTTON_INIT));

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult  .......");
        setProgressBarGone();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {

                //subscribe();
            }
        }
    }

    private void judGmentGoogleFit() {
        final FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                        .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                        .build();

        //查询当天的每个一个小时的数据；
        FitnessOptions fitnessOptions2 =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                        //.addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                        .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions2)) {
            addWindow();
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions2);

        } else {
            //subscribe();
        }

    }

    /**
     * 查询当天的完整   订阅后service 才能读取数据；
     */
    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed steps !");
                                    setProgressBarGone();
                                } else {
                                    Log.w(TAG, "There was a problem subscribing. steps ", task.getException());
                                }
                            }
                        });


        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_CALORIES_EXPENDED)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed  calor!");
                                    setProgressBarGone();
                                } else {
                                    Log.w(TAG, "There was a problem subscribing. calor", task.getException());
                                }
                            }
                        });

        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_DISTANCE_DELTA
                )
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    setProgressBarGone();
                                    Log.i(TAG, "Successfully subscribed  distance !");
                                } else {
                                    Log.w(TAG, "There was a problem subscribing. distannce ", task.getException());
                                }
                            }
                        });

        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_ACTIVITY_SEGMENT)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed  calor!");
                                } else {
                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }


    private void readData() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                long total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                Log.i(TAG, "Total steps: " + total);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the step count.", e);
                            }
                        });


        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        Float total =
                                dataSet.isEmpty()
                                        ? 0f
                                        : dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
                        Log.i(TAG, "Total calories: " + total);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "There was a problem getting the step calories.", e);
                    }
                });

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        Float total =
                                dataSet.isEmpty()
                                        ? 0f
                                        : dataSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).asFloat();
                        Log.i(TAG, "Total distance : " + total);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "There was a problem getting the step distance .", e);
                    }
                });

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        Float total =
                                dataSet.isEmpty()
                                        ? 0f
                                        : dataSet.getDataPoints().get(0).getValue(Field.FIELD_ACTIVITY).asFloat();
                        Log.i(TAG, "Total activitytiem  : " + total);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "There was a problem getting the step activitytime  .", e);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    private void initView() {
        //mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTablayout = (TabLayout) findViewById(R.id.tablayout);
        mViepager = (ViewPager) findViewById(R.id.viepager);
        mTablayout.setTabMode(TabLayout.MODE_FIXED);


        view_mainView = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        pop_view = LayoutInflater.from(this).inflate(R.layout.pop_permision_dialog, null);
        TextView upfindViewById = pop_view.findViewById(R.id.update_setting);
        //upfindViewById.setOnClickListener(this);
        //upfindViewById.setClickable(true);
        // pop_view.findViewById(R.id.close_icon).setOnClickListener(this);

        popProgressBarView = RelativeLayout.inflate(this, R.layout.progress_loading, null);
        contentLoadingProgressBar = popProgressBarView.findViewById(R.id.ppppp);
        contentLoadingProgressBar.show();



        timeline_float = findViewById(R.id.timeline);
        reset_float = findViewById(R.id.reset);
        menuDown = (FloatingActionMenu) findViewById(R.id.menu_down);


        imageView = findViewById(R.id.sport_time);
        imageView.setOnClickListener(this);

    }

    private void initDate() {

        sp = new SharedPreferencesUtils(this);
        //获取用户设置的计划锻炼步数，没有设置过的话默认7000
        String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "7000");
        //设置当前步数为0


        myDbUtils = new MyDbUtils(this);
        daoSession = myDbUtils.getDaoManager().getDaoSession();
    }

    private String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * 登陆账号进度圈 ；
     */
    public void addWindow() {

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        //layoutParams.gravity = Gravity.CENTER;
        getWindowManager().addView(popProgressBarView, layoutParams);


        //new AlertDialog.Builder(this).setMessage("asdfadfafd").create().show();
    }


    private void setViewPager() {
        ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
        pedometerFragment = new PedometerFragment();
        reportFragment = new ReportFragment();

        MultiReportFragment multiReportFragment = new MultiReportFragment();
        SettingFragment settingFragment = new SettingFragment();
        fragmentArrayList.add(pedometerFragment);
        //fragmentArrayList.add(reportFragment);
        fragmentArrayList.add(multiReportFragment);
        fragmentArrayList.add(settingFragment);

        ArrayList<String> arrayListTitle = new ArrayList<String>();
        arrayListTitle.add(getResources().getString(R.string.today));
        arrayListTitle.add(getResources().getString(R.string.report));
        arrayListTitle.add(getResources().getString(R.string.setting));

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), arrayListTitle, fragmentArrayList);
        mViepager.setAdapter(viewPagerAdapter);
        mTablayout.setupWithViewPager(mViepager);
        // mViepager.setOffscreenPageLimit(3);
        pedometerFragment.registerPedometerFragmentListener(new PedometerFragmentListener() {
            @Override
            public void setCurrent(int fragment, String tag) {
                mViepager.setCurrentItem(fragment);
                switch (tag) {
                    case "kcal":
                        mViepager.setCurrentItem(fragment);
                        reportFragment.performClickReportTv(true, CALORIE_NUMBER);
                        break;
                    case "distance":
                        mViepager.setCurrentItem(fragment);
                        reportFragment.performClickReportTv(true, DISTANCE_NUMBER);
                        break;
                    case "time":
                        mViepager.setCurrentItem(fragment);
                        reportFragment.performClickReportTv(true, TIME_NUMBER);
                        break;
                }
            }
        });

       mViepager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
           @Override
           public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//               if (position!=0){
//                   //menuDown.hideMenu(true);
//                   menuDown.setVisibility(View.GONE);
//               }else {
//                   menuDown.hideMenu(false);
//                   menuDown.setVisibility(View.VISIBLE);
//               }
           }

           @Override
           public void onPageSelected(int position) {
               if (position!=0){
                   //menuDown.hideMenu(true);
                   menuDown.setVisibility(View.GONE);
               }else {
                   menuDown.hideMenu(false);
                   menuDown.setVisibility(View.VISIBLE);
               }
           }

           @Override
           public void onPageScrollStateChanged(int state) {
               //menuDown.hideMenu(true);
               //menuDown.setVisibility(View.GONE);
           }
       });
    }

    public Fragment getPedometerFragment() {
        if (pedometerFragment != null) {
            return pedometerFragment;
        } else return null;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            setProgressBarGone();
            Log.d(TAG, "on keyDown...........");

            // 当天只弹出一次；
            if (whetherStartPop() && !sp.getParam(SharedPreferencesUtils.ISNEWDAY, "han").equals(getTodayDate())) {
                sp.setParam(SharedPreferencesUtils.ISNEWDAY, getTodayDate());
                popWindow(pop_view);
            }
            //doubleClickFinish();
            return true;
        }*/
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            setProgressBarGone();
            Log.d(TAG, "on keyDown......home.....");
            return true;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                break;

        }
        return super.onKeyDown(keyCode, event);
    }


    /*监听home 键*/
    class HomeWatchReceiver extends BroadcastReceiver {
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String shortHome = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(shortHome)) {

                    //短按home 键；
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        setProgressBarGone();
        if (!BackHandlerHelper.handleBackPress(this)) {
            //super.onBackPressed();
            setProgressBarGone();
            Log.d(TAG, "on keyDown...........");

            // 当天只弹出一次；
            if (whetherStartPop() && !sp.getParam(SharedPreferencesUtils.ISNEWDAY, "han").equals(getTodayDate())) {
                sp.setParam(SharedPreferencesUtils.ISNEWDAY, getTodayDate());
                popWindow(pop_view);
            }
            doubleClickFinish();
        }

        //super.onBackPressed();
    }

    /**
     * 双击退出
     */
    private void doubleClickFinish() {

        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastTime) < 1200) {
            finish();
        } else {
            Toast.makeText(this, getString(R.string.back_toast), Toast.LENGTH_SHORT).show();
            lastTime = System.currentTimeMillis();
        }

    }


    private void popWindow(View view_pop) {


        //显示的布局，还可以通过设置一个View
// dp
//是否获取焦点，默认为ture
//是否PopupWindow 以外触摸dissmiss
        popWindow = new CustomPopWindow.PopupWindowBuilder(this)
                .setView(view_pop)//显示的布局，还可以通过设置一个View
                .enableBackgroundDark(true)
                .size(310, 350) // dp
                .setBgDarkAlpha(0.5f)
                .setFocusable(true)//是否获取焦点，默认为ture
                .setOutsideTouchable(true)//是否PopupWindow 以外触摸dissmiss
                .create();
        pop_textViewSubTitle = view_pop.findViewById(R.id.sub_title);
        String appName = getString(R.string.app_name);
        pop_textViewSubTitle.setText(String.format(getResources().getString(R.string.pg_permission_dialog_sub_title), appName));
        popWindow.showAtLocation(view_mainView, Gravity.CENTER, 0, 0);//显示PopupWindow
    }

    private void setToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setElevation(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(0);
        }
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_set:
                        /*FireBase统计*/
                        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdAppHelper.getInstance(getApplicationContext()).getNative(0, adFrameLayout, params);
        Log.d(TAG, " Resume >>>>>>>>>>>");
        new Handler().post(new Runnable() {
            @Override
            public void run() {
            }
        });


    }


    public void setProgressBarGone() {
        if (contentLoadingProgressBar != null) {
            popProgressBarView.setVisibility(View.GONE);
            contentLoadingProgressBar.hide();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setProgressBarGone();
        Log.d(TAG, "onSaveInstanceState.............");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "pause>>>>>>>>>>");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "mainactivity  finish ");
        setProgressBarGone();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_setting:
                // TODO: 2017/12/21
                //Intent intent = new Intent();
                //intent.setAction(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                settingBatteryPower();
                popWindow.dissmiss();
                break;
            case R.id.close_icon:
                popWindow.dissmiss();
                break;

            case R.id.sport_time:
                Intent intent = new Intent(MainActivity.this , SportStepFormalActivity.class );
                startActivity(intent);
                break;
        }


    }

    public void ViewClick(View view) {
        switch (view.getId()) {
            case R.id.update_setting:
                settingBatteryPower();
                popWindow.dissmiss();
                break;

            case R.id.close_icon:
                popWindow.dissmiss();
        }
    }


    private void settingBatteryPower() {
        // 当前设备的系统版本；

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = this.getPackageName();
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                //intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                //intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                //intent.setAction(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                //intent.setData(Uri.parse("package:" + packageName));
                //startActivity(intent);

                if (OSUtils.getSystem() == OSUtils.SYS_MIUI) {
                    //跳转miui 神隐模式；
                    intent.setComponent(new ComponentName("com.miui.powerkeeper",
                            "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"));
                    intent.putExtra("package_name", packageName);
                    intent.putExtra("package_label", "Pedometer");

                } else {
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                }
                startActivity(intent);
            }
        }

    }

    private boolean whetherStartPop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = this.getPackageName();
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

}
