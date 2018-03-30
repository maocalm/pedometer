package com.baisi.spedometer.step.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.baisi.spedometer.MainActivity;
import com.baisi.spedometer.R;
import com.baisi.spedometer.evenbus.Event;
import com.baisi.spedometer.evenbus.EventBusUtil;
import com.baisi.spedometer.greendao.gen.DaoSession;
import com.baisi.spedometer.greendao.gen.StepDataDao;
import com.baisi.spedometer.step.UpdateUiCallBack;
import com.baisi.spedometer.step.accelerometer.StepCount;
import com.baisi.spedometer.step.accelerometer.StepValuePassListener;
import com.baisi.spedometer.step.bean.StepData;
import com.baisi.spedometer.step.utils.DaoManager;
import com.baisi.spedometer.step.utils.DownTimer;
import com.baisi.spedometer.step.utils.MyDbUtils;
import com.baisi.spedometer.step.utils.SharedPreferencesUtils;
import com.baisi.spedometer.step.utils.StepConversion;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;


public class StepService extends Service implements SensorEventListener {
    private String TAG = "StepService";
    /**
     * 默认为30秒进行一次存储
     */
    private static int duration = 30 * 1000;

    /**
     * 当前的日期
     */
    private static Date CURRENT_DATE;
    /**
     * 传感器管理对象
     */
    private SensorManager sensorManager;
    /**
     * 广播接受者
     */
    private BroadcastReceiver mBatInfoReceiver;
    /**
     * 保存记步计时器
     */
    private TimeCount time;
    /**
     * 当前所走的步数
     */
    private int CURRENT_STEP;
    /**
     * 计步传感器类型  Sensor.TYPE_STEP_COUNTER或者Sensor.TYPE_STEP_DETECTOR
     */
    private static int stepSensorType = -1;
    /**
     * 每次第一次启动记步服务时是否从系统中获取了已有的步数记录
     */
    private boolean hasRecord = false;
    /**
     * 系统中获取到的已有的步数
     */
    private int hasStepCount = 0;
    /**
     * 上一次的步数
     */
    private int previousStepCount = 0;
    /**
     * 通知管理对象
     */
    private NotificationManager mNotificationManager;
    /**
     * 加速度传感器中获取的步数
     */
    private StepCount mStepCount;
    /**
     * IBinder对象，向Activity传递数据的桥梁
     */
    private StepBinder stepBinder = new StepBinder();
    /**
     * 通知构建者
     */
    private NotificationCompat.Builder mBuilder;
    private Sensor sensor_acceleremeter;

    /*计时器的总时长和时间间隔*/
    private static int STEPTIMECOUNTSECOND_MILLISINFUTURE = 10000;
    private static int STEPTIMECOUNTSECOND_COUNTDOWNINTERVAL = 1000;

    /*时间计算*/
    private StepTimeCountSecond mStepTimerCount;

    /*当天所走的秒数*/
    private int stepTimeCountSecond = 0;

    /*传感器类型； */
    private Sensor countSensor;
    private Sensor detectorSensor;

    /*是否正在计时； */
    private boolean iscomputeTime = true;

    private SharedPreferencesUtils sharedPreferencesUtils;

    private Handler handler = new Handler();
    private long timeOfLastPeak = 0;
    private long timeOfThisPeak = 0;
    private DownTimer downTimer;

    //  存储收到计步回调时间；
    private ArrayList<Long> longArrayList;
    private DateFormat dateFormat;


    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

    static boolean googlefitOnUpdaUi = false;  // 控制googlefit 停止暂停更新ui ;
    private SimpleDateFormat simpleDateFormat;
    private DaoSession daoSession;

    //上个小时的步骤和运动时间 ；
    private int lastHourStepAll = 0;
    private int lastHourStepSecondAll = 0;

    private int stepSecondDruction = 5 * 1000;

    // 登陆googlefit  多长时间访问googlefit数据库
    private int googlefitSecond = 10 * 1000;

    // sprot  运动模式步骤 ；
    private int SPORTSTEPCOUNT;
    private boolean  isSendHeartBeat = false ;

    @Override
    public void onCreate() {
        EventBusUtil.register(this);
        super.onCreate();
        Log.d(TAG, "onCreate()");
        sharedPreferencesUtils = new SharedPreferencesUtils(StepService.this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //initStepCountTime();
        simpleDateFormat = new SimpleDateFormat();
        initNotification();
        initTodayData();
        initBroadcastReceiver();

        startTimeCount();

        new Thread(new Runnable() {
            public void run() {
                startStepDetector();
            }
        }).start();
        //initStepCountTimeDownTimer();
        startStepCountThread();

        readFitData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventReceive(Event event) {
        switch (event.getCode()) {
            case EventBusUtil.EventCode.STOPNOTIFICATION:
                //mNotificationManager.cancel(notifyId_Step);
                mNotificationManager.cancelAll();
                stopForeground(true);

                Log.d(TAG, " +++receive message envetbus +++");
                break;
            case EventBusUtil.EventCode.STARTNOTIFICATION:

                break;
            case EventBusUtil.EventCode.RESTSTEPDATA:
                CURRENT_STEP = 0;
                stepTimeCountSecond = 0;
                sharedPreferencesUtils.setParam(SharedPreferencesUtils.LAST_HOURSTEP_ALL, 0);
                sharedPreferencesUtils.setParam(SharedPreferencesUtils.LAST_HOURSTEPSECOND_ALL, 0);
                break;
            case EventBusUtil.EventCode.SPORTMODESTART:
                isSendHeartBeat = true ;
                break;
            case EventBusUtil.EventCode.SPORTMODEPAUSE:
                isSendHeartBeat = false ;
                break;
            case EventBusUtil.EventCode.SPORTMODESTOP:
                isSendHeartBeat = false ;
                break;
        }
    }

    Runnable readDateRunnable = new Runnable() {
        @Override
        public void run() {

            if (judGmentGoogleFit()) {
                //readData();
                readHistoryData();
            }
            handler.postDelayed(readDateRunnable, googlefitSecond);
        }
    };


    private void readFitData() {
        handler.postDelayed(readDateRunnable, googlefitSecond);

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

        //查询时刻step
        FitnessOptions fitnessOptions2 =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                        .build();

        if (GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(StepService.this), fitnessOptions2)) {
            googlefitOnUpdaUi = true;
            return true;
        } else {
            return false;
        }

    }


    public DataReadRequest queryFitnessData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        //cal.add(Calendar.HOUR_OF_DAY, -16);
        //long startTime = cal.getTimeInMillis();
        long startTime = StepConversion.getZero(now);

        java.text.DateFormat dateFormat = getDateInstance();
        Log.d(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.d(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest =
                new DataReadRequest.Builder()
                        // The data request can specify multiple data types to return, effectively
                        // combining multiple data queries into one call.
                        // In this example, it's very unlikely that the request is for several hundred
                        // datapoints each consisting of a few steps and a timestamp.  The more likely
                        // scenario is wanting to see how many steps were walked per day, for 7 days.

                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)

                        // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                        // bucketByTime allows for a time span, whereas bucketBySession would allow
                        // bucketing by "sessions", which would need to be defined in code.
                        //.aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                        .bucketByTime(1, TimeUnit.HOURS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();
        // [END build_read_data_request]

        return readRequest;
    }

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
                                    Log.i(TAG, "Successfully subscribed  distance !");
                                } else {
                                    Log.w(TAG, "There was a problem subscribing. distannce ", task.getException());
                                }
                            }
                        });

      /*Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
              .subscribe(DataType.)
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
                      });*/
    }

    /**
     * 读取googlefit 历史数据间隔一个小时；
     * Asynchronous task to read the history data. When the task succeeds, it will print out the data.
     */
    private Task<DataReadResponse> readHistoryData() {
        // Begin by creating the query.
        DataReadRequest readRequest = queryFitnessData();

        // Invoke the History API to fetch the data with the query
        return Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                // For the sake of the sample, we'll print the data so we can see what we just
                                // added. In general, logging fitness information should be avoided for privacy
                                closeReceiverSenserChanger();
                                // reasons.

                                List<StepData> stepDataList = daoSession.getStepDataDao().queryBuilder()
                                        .where(StepDataDao.Properties.StepToday.eq(getTodaySimpleDate())).list();

                                for (int i = 0; i < stepDataList.size(); i++) {
                                    StepData stepData = stepDataList.get(i);
                                    daoSession.getStepDataDao().delete(stepData);
                                }


                                stepsGooglefitDateSet = 0;
                                for (int i = 0; i < dataReadResponse.getBuckets().size(); i++) {
                                    List<DataSet> dataSets = dataReadResponse.getBuckets().get(i).getDataSets();
                                    for (int i1 = 0; i1 < dataSets.size(); i1++) {
                                        getDataPonitAndSave(dataSets.get(i1));
                                    }
                                }
                                CURRENT_STEP = stepsGooglefitDateSet;
                                stepTimeCountSecond = stepsGooglefitDateSet / 2;
                                updateNotification();

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "There was a problem reading the data.", e);
                            }
                        });
    }


    /**
     * 从google fit 获取填充数据库的数据源 >>>填充数据库；
     *
     * @param dataSet
     */
    int stepsGooglefitDateSet = 0; //从googlefit 中区的总的步数

    private void getDataPonitAndSave(DataSet dataSet) {
        List<DataPoint> dataPointList = dataSet.getDataPoints();

        List<Integer> dataPointSteplist = new ArrayList<>();
        for (int i = 0; i < dataPointList.size(); i++) {
            List<Field> fieldList = dataPointList.get(i).getDataType().getFields();
            int stepCount = 0;
            for (int i1 = 0; i1 < fieldList.size(); i1++) {
                stepCount = dataPointList.get(i).getValue(fieldList.get(i1)).asInt();

            }
            dataPointSteplist.add(stepCount);
        }

        int daoSize = daoSession.getStepDataDao().loadAll().size();
        Log.d(TAG, "getDataPonitAndSave: daosize" + daoSize);
        for (int i = 0; i < dataPointList.size(); i++) {
            StepData stepData = new StepData();
            int step = dataPointSteplist.get(i);
            Long stepDataTime = dataPointList.get(i).getStartTime(TimeUnit.MILLISECONDS);
            stepData.setStep(step);
            //stepData.setId(daoSize+1L);
            stepData.setReset("false");
            stepData.setStepToday(getTodaySimpleDate());
            stepData.setStepSecond(step / 2);
            stepData.setStepTime(new Date(stepDataTime));
            daoSession.getStepDataDao().insertOrReplace(stepData);
            Log.d(TAG, "getDataPonitAndSave: step" + step);
            stepsGooglefitDateSet += step;
        }


    }

    /**
     * 获取googfit 当天的step步数总
     */
    private void readData() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {

                                // 关闭我们的传感器接受；
                                closeReceiverSenserChanger();
                                if (dataSet != null) {
                                    int total =
                                            dataSet.isEmpty()
                                                    ? 0
                                                    : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                    Log.i(TAG, "  ggoogleFit Total steps: " + total);

                                    if (googlefitOnUpdaUi) {
                                        CURRENT_STEP = total;
                                        stepTimeCountSecond = total / 2;
                                        updateNotification();
                                        save();
                                    }
                                }

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the  googlefit  step count.", e);
                            }
                        });


        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {

                        if (dataSet != null) {
                            Float total =
                                    dataSet.isEmpty()
                                            ? 0f
                                            : dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
                            Log.i(TAG, "Total calories: " + total);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "There was a problem getting the  googlefit  step calories.", e);
                    }
                });

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {

                        if (dataSet != null) {

                            Float total =
                                    dataSet.isEmpty()
                                            ? 0f
                                            : dataSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).asFloat();
                            Log.i(TAG, "Total distance : " + total);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "There was a problem getting the googlefit   step distance .", e);
                    }
                });

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {

                        if (dataSet != null) {
                            Long totalstart =
                                    dataSet.isEmpty()
                                            ? 0L
                                            : dataSet.getDataPoints().get(0).getStartTime(TimeUnit.SECONDS);
                            Long totalend =
                                    dataSet.isEmpty()
                                            ? 0L
                                            : dataSet.getDataPoints().get(0).getStartTime(TimeUnit.SECONDS);
                            Log.i(TAG, "Total activitytime : " + (totalend - totalstart));
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "There was a problem getting  googlefit   the step distance .", e);
                    }
                });
    }

    private void startStepCountThread() {
        longArrayList = new ArrayList();
        Thread thread = new Thread() {

            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        Thread.sleep(stepSecondDruction);
                        long a = System.currentTimeMillis();

                        if (!longArrayList.isEmpty()) {

                            if (a > 2000 + longArrayList.get(longArrayList.size() - 1)) {
                                //todo  三秒没收到回调
                                longArrayList.clear();
                            }
                        }
                        if (!longArrayList.isEmpty()) {

                            int steptime;
                            steptime = Integer.parseInt(String.valueOf((longArrayList.get(longArrayList.size() - 1) - longArrayList.get(0)) / 1000));
                            stepTimeCountSecond = stepTimeCountSecond + steptime;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("aaa", "up date ui  steptimecountsecond :" + CURRENT_STEP);
                                    if (mCallback != null) {
                                        mCallback.updateUi(CURRENT_STEP, stepTimeCountSecond);
                                    }
                                }
                            });
                            //save();
                            longArrayList.clear();
                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


        };
        thread.start();
    }

    public void initStepCountTimeDownTimer() {
        downTimer = new DownTimer();
        downTimer.setTotalTime(30 * 1000);
        downTimer.setIntervalTime(1000);
        downTimer.setTimerLiener(new DownTimer.TimeListener() {
            @Override
            public void onFinish() {
                stepTimeCountSecond = stepTimeCountSecond + 30;
                //save();
                mCallback.updateUi(CURRENT_STEP, stepTimeCountSecond); //  时间结束即刻更新
                //updateNotification();
                Log.d(TAG, " steptimecountsecond   downtimer   save   :" + stepTimeCountSecond + " save ");
                downTimer.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downTimer.pause();
                    }
                }, 800);


            }

            @Override
            public void onInterval(long remainTime) {
                Log.d(TAG, " steptimecountsecond   downtimer   remain   :" + remainTime);

            }
        });

        downTimer.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                downTimer.pause();
            }
        }, 800);
    }

    public void initStepCountTime() {
        mStepTimerCount = new StepTimeCountSecond(STEPTIMECOUNTSECOND_MILLISINFUTURE, STEPTIMECOUNTSECOND_COUNTDOWNINTERVAL);
    }

    /**
     * 获取当天日期
     *
     * @return
     */
    private Date getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);*/
        return date;
    }

    private String getTodaySimpleDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * 初始化通知栏
     */
    private void initNotification() {
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getString(R.string.notification_content) + CURRENT_STEP + getString(R.string.kong_ge) + getString(R.string.steps))
                .setContentIntent(getDefalutIntent(Notification.FLAG_ONGOING_EVENT))
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
                .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
                .setAutoCancel(false)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(true)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources() ,R.drawable.icon_step))
                .setSmallIcon(R.mipmap.ic_stat_title)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        Notification notification = mBuilder.build();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (sharedPreferencesUtils.getParam(SharedPreferencesUtils.SWITCHBAR, SharedPreferencesUtils.SWITCHBAR_DEFAULT).equals(SharedPreferencesUtils.SWITCH_TRUE)) {
            startForeground(notifyId_Step, notification);
        }
        Log.d(TAG, "initNotification()");
    }

    /**
     * 初始化当天的步数
     */
    private void initTodayData() {
        CURRENT_DATE = getTodayDate();
        MyDbUtils myDbUtils = new MyDbUtils(this);
        daoSession = myDbUtils.getDaoManager().getDaoSession();
        //获取当天的数据，用于展示
        List<StepData> list = daoSession.getStepDataDao().queryBuilder().where(StepDataDao.Properties.StepToday.eq(getTodaySimpleDate())).list();
        Date todayDate = null;
        Log.d(TAG, "initToday  isempty out ");
        if (list.size() == 0 || list.isEmpty()) {
            CURRENT_STEP = 0;
            stepTimeCountSecond = 0;
            sharedPreferencesUtils.setParam(SharedPreferencesUtils.LAST_HOURSTEP_ALL, 0);
            sharedPreferencesUtils.setParam(SharedPreferencesUtils.LAST_HOURSTEPSECOND_ALL, 0);
            Log.d(TAG, "initToday");

            // 第一天数据为空清空数据库，重建；
            todayDate = new Date();
            Log.d(TAG, "inittodaydate  deleteDB   out ");
            if (StepConversion.getDayofweek(todayDate) == 1) {
               /* Log.d(TAG, "inittodaydate  delete and create DB");
                if (DbUtils.getLiteOrm() != null) {
                    DaoManager.getInstance().init().getLiteOrm().deleteDatabase();
                    DbUtils.setLiteOrmNull();
                }
                DbUtils.createDb(StepService.this, "SpedometerStepCount");*/
            }
        } else if (list.size() > 0) {
            Log.d(TAG, "inittodaydate  list size  == 1  ");
            Log.d(TAG, "initTodayData()   listtoday :  " + list.toString());
            int stepCountAll = 0;
            int stepSecontAll = 0;
            for (int i = 0; i < list.size(); i++) {
                stepCountAll += list.get(i).getStep();
                stepSecontAll += list.get(i).getStepSecond();

                sharedPreferencesUtils.setParam(SharedPreferencesUtils.LAST_HOURSTEP_ALL, 0);
                sharedPreferencesUtils.setParam(SharedPreferencesUtils.LAST_HOURSTEPSECOND_ALL, 0);
            }
            CURRENT_STEP = stepCountAll;
            stepTimeCountSecond = stepSecontAll;

            Log.d(TAG, "initTodayData: CURRENT_STEP:" + CURRENT_STEP + "stepTimeCountSecond :" + stepTimeCountSecond);
        } else {
            Log.v(TAG, "出错了！");
        }
        if (mStepCount != null) {
            mStepCount.setSteps(CURRENT_STEP);
        }
        updateNotification();
    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //关机广播
        filter.addAction(Intent.ACTION_SHUTDOWN);
        // 屏幕亮屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        // 屏幕解锁广播
//        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //监听日期变化
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);

        mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    Log.d(TAG, "screen on");
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    Log.d(TAG, "screen off");
                    //改为60秒一存储
                    duration = 60000;
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    Log.d(TAG, "screen unlock");
                    // save();
                    //改为30秒一存储
                    duration = 30000;
                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    Log.i(TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
                    //保存一次
                    //save();
                } else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                    Log.i(TAG, " receive ACTION_SHUTDOWN");
                    //save();
                } else if (Intent.ACTION_DATE_CHANGED.equals(action)) {//日期变化步数重置为0
                    //Logger.d("重置步数" + StepDcretor.CURRENT_STEP);
                    //save();
                    isNewDay();
                } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
                    //时间变化步数重置为0
                    isCall();
                    //save();
                    //isNewDay();
                } else if (Intent.ACTION_TIME_TICK.equals(action)) {//日期变化步数重置为0   分钟
                    //isCall();
                    // Logger.d("重置步数" + StepDcretor.CURRENT_STEP);
                    //save();
                    //isNewDay();
                }
            }
        };
        registerReceiver(mBatInfoReceiver, filter);
    }


    /**
     * 监听晚上0点变化初始化数据
     */
    private void isNewDay() {
        //isNewWeek();

        Log.d(TAG, "isNewDay: ");
        String time = "00:00";
        if (time.equals(new SimpleDateFormat("HH:mm").format(new Date())) || !CURRENT_DATE.equals(getTodayDate())) {
            initTodayData();
        }

    }

    private void isNewWeek() {
        // is a new week;
        String time = "00:00";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (time.equals(new SimpleDateFormat("HH:mm").format(new Date())) || !CURRENT_DATE.equals(getTodayDate())) {
            if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
                Log.d(TAG, "is a new week delete db");
               /* DbUtils.deleteAll(StepData.class);
                DbUtils.getLiteOrm().update(StepData.class);*/
                //DbUtils.getLiteOrm().deleteDatabase();
                // DbUtils.setLiteOrmNull();


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "is a new week creat db");
                        // DbUtils.createDb(StepService.this, DbUtils.DB_NAME);
                    }
                }, 2000);
            }
        }
    }

    /**
     * 监听时间变化提醒用户锻炼
     */
    private void isCall() {
        String time = this.getSharedPreferences("share_date", Context.MODE_MULTI_PROCESS).getString("achieveTime", "21:00");
        String plan = this.getSharedPreferences("share_date", Context.MODE_MULTI_PROCESS).getString("planWalk_QTY", "7000");
        String remind = this.getSharedPreferences("share_date", Context.MODE_MULTI_PROCESS).getString("remind", "1");
        Log.d("step", "time=" + time + "\n" +
                "new SimpleDateFormat(\"HH: mm\").format(new Date()))=" + new SimpleDateFormat("HH:mm").format(new Date()));
        if (("1".equals(remind)) &&
                (CURRENT_STEP < Integer.parseInt(plan)) &&
                (time.equals(new SimpleDateFormat("HH:mm").format(new Date())))
                ) {
            remindNotify();
        }

    }

    /**
     * 开始保存记步数据
     */
    private void startTimeCount() {
        if (time == null) {
            time = new TimeCount(duration, 1000);
        }
        time.start();
    }

    /*开始保存跑步时间*/
    private void startStepTimeCountSecond() {
        if (mStepTimerCount == null) {
            mStepTimerCount = new StepTimeCountSecond(STEPTIMECOUNTSECOND_MILLISINFUTURE, STEPTIMECOUNTSECOND_COUNTDOWNINTERVAL);
        }
        mStepTimerCount.start();
    }

    /**
     * 更新步数通知
     */
    private void updateNotification() {
        //设置点击跳转
        Intent hangIntent = new Intent(this, MainActivity.class);
        PendingIntent hangPendingIntent = PendingIntent.getActivity(this, 0, hangIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = mBuilder.setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getString(R.string.notification_content) + CURRENT_STEP + getString(R.string.kong_ge) + getString(R.string.steps))
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
                .setContentIntent(hangPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        if (sharedPreferencesUtils.getParam(SharedPreferencesUtils.SWITCHBAR, SharedPreferencesUtils.SWITCHBAR_DEFAULT).toString().equals("1")) {
            mNotificationManager.notify(notifyId_Step, notification);
            Log.d(TAG, "   notificationManager ");
        }


        if (mCallback != null) {
            Log.d("aaa", "updatauiCallback !=null  and   update step is  " + CURRENT_STEP);
            mCallback.updateUi(CURRENT_STEP, stepTimeCountSecond);
        }
        Log.d(TAG, "updateNotification()");

    }

    /**
     * UI监听器对象
     */
    private UpdateUiCallBack mCallback;

    /**
     * 注册UI更新监听
     *
     * @param paramICallback
     */
    public void registerCallback(UpdateUiCallBack paramICallback) {
        this.mCallback = paramICallback;
    }

    /**
     * 记步Notification的ID
     */
    int notifyId_Step = 100;
    /**
     * 提醒锻炼的Notification的ID
     */
    int notify_remind_id = 200;

    /**
     * 提醒锻炼通知栏
     */
    private void remindNotify() {

        //设置点击跳转
        Intent hangIntent = new Intent(this, MainActivity.class);
        PendingIntent hangPendingIntent = PendingIntent.getActivity(this, 0, hangIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        String plan = this.getSharedPreferences("share_date", Context.MODE_MULTI_PROCESS).getString("planWalk_QTY", "7000");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("今日步数" + CURRENT_STEP + " 步")
                .setContentText("距离目标还差" + (Integer.valueOf(plan) - CURRENT_STEP) + "步，加油！")
                .setContentIntent(hangPendingIntent)
                .setTicker(getResources().getString(R.string.app_name) + "提醒您开始锻炼了")//通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
                .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
                .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
        //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
        //.setSmallIcon(R.mipmap.logo);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //mNotificationManager.notify(notify_remind_id, mBuilder.build());
    }

    /**
     * @获取默认的pendingIntent,为了防止2.3及以下版本报错
     * @flags属性: 在顶部常驻:Notification.FLAG_ONGOING_EVENT
     * 点击去除： Notification.FLAG_AUTO_CANCEL
     */
    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stepBinder;
    }

    /**
     * 向Activity传递数据的纽带
     */
    public class StepBinder extends Binder {

        /**
         * 获取当前service对象
         *
         * @return StepService
         */
        public StepService getService() {
            return StepService.this;
        }
    }

    /**
     * 获取当前步数
     *
     * @return
     */
    public int getStepCount() {
        return CURRENT_STEP;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initNotification();
        Log.d(TAG, "onstartConmmend");
        initTodayData();
        return START_STICKY;
    }

    /**
     * 获取传感器实例
     */
    private void startStepDetector() {
        if (sensorManager != null) {
            sensorManager = null;
        }
        // 获取传感器管理器的实例
        sensorManager = (SensorManager) this
                .getSystemService(SENSOR_SERVICE);
        //android4.4以后可以使用计步传感器
        int VERSION_CODES = Build.VERSION.SDK_INT;
        if (VERSION_CODES >= 19) {
            addCountStepListener();
        } else {
            addBasePedometerListener();
        }
    }

    /**
     * 添加传感器监听
     * 1. TYPE_STEP_COUNTER API的解释说返回从开机被激活后统计的步数，当重启手机后该数据归零，
     * 该传感器是一个硬件传感器所以它是低功耗的。
     * 为了能持续的计步，请不要反注册事件，就算手机处于休眠状态它依然会计步。
     * 当激活的时候依然会上报步数。该sensor适合在长时间的计步需求。
     * <p>
     * 2.TYPE_STEP_DETECTOR翻译过来就是走路检测，
     * API文档也确实是这样说的，该sensor只用来监监测走步，每次返回数字1.0。
     * 如果需要长事件的计步请使用TYPE_STEP_COUNTER。
     */
    private void addCountStepListener() {
        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        countSensor = null;
        detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_COUNTER;
            Log.v(TAG, "Sensor.TYPE_STEP_COUNTER");
            sensorManager.registerListener(StepService.this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (detectorSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_DETECTOR;
            Log.v(TAG, "Sensor.TYPE_STEP_DETECTOR");
            sensorManager.registerListener(StepService.this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.v(TAG, "Count sensor not available!");
            addBasePedometerListener();
        }
    }

    /**
     * 传感器监听回调
     * 记步的关键代码
     * 1. TYPE_STEP_COUNTER API的解释说返回从开机被激活后统计的步数，当重启手机后该数据归零，
     * 该传感器是一个硬件传感器所以它是低功耗的。
     * 为了能持续的计步，请不要反注册事件，就算手机处于休眠状态它依然会计步。
     * 当激活的时候依然会上报步数。该sensor适合在长时间的计步需求。
     * <p>
     * 2.TYPE_STEP_DETECTOR翻译过来就是走路检测，
     * API文档也确实是这样说的，该sensor只用来监监测走步，每次返回数字1.0。
     * 如果需要长事件的计步请使用TYPE_STEP_COUNTER。
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.e("aaaaaa", "onSensorChanged: --->"+ event.timestamp);
        if (stepSensorType == Sensor.TYPE_STEP_COUNTER) {
            //获取当前传感器返回的临时步数
            int tempStep = (int) event.values[0];
            //首次如果没有获取手机系统中已有的步数则获取一次系统中APP还未开始记步的步数
            if (!hasRecord) {
                hasRecord = true;
                hasStepCount = tempStep;
            } else {
                //获取APP打开到现在的总步数=本次系统回调的总步数-APP打开之前已有的步数
                int thisStepCount = tempStep - hasStepCount;
                //本次有效步数=（APP打开后所记录的总步数-上一次APP打开后所记录的总步数）
                int thisStep = thisStepCount - previousStepCount;
                //总步数=现有的步数+本次有效步数
                CURRENT_STEP += (thisStep);
                //记录最后一次APP打开到现在的总步数
                previousStepCount = thisStepCount;
            }
            Log.d(TAG, "tempStep" + tempStep);
            Log.d(TAG, "CURRENT_STEP" + CURRENT_STEP);
            Log.d(TAG, "previousStepCount" + previousStepCount);
            Log.d(TAG, "TYPE_STEP_COUNTER");
        } else if (stepSensorType == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0) {
                CURRENT_STEP++;
                Log.d(TAG, "TYPE_STEP_DETECTOR");
                if (isSendHeartBeat){
                    SPORTSTEPCOUNT++ ;
                    EventBusUtil.sendStickyEvent(new Event(EventBusUtil.EventCode.SPORTMODE_SERVICE_TO_SPORTACTIVITY , SPORTSTEPCOUNT) );
                }
            }
        }
        updateNotification();
        startUpDateStepTimeCountSecond();

    }


    /**
     * 通过加速度传感器来记步
     */
    private void addBasePedometerListener() {
        mStepCount = new StepCount();
        mStepCount.setSteps(CURRENT_STEP);
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        sensor_acceleremeter = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        boolean isAvailable = sensorManager.registerListener(mStepCount.getStepDetector(), sensor_acceleremeter,
                SensorManager.SENSOR_DELAY_UI);
        mStepCount.initListener(new StepValuePassListener() {
            @Override
            public void stepChanged(int steps) {
                CURRENT_STEP = steps;
                startUpDateStepTimeCountSecond();
                updateNotification();
                Log.d(TAG, " 步骤是 ————：" + steps);
            }
        });
        if (isAvailable) {
            Log.v(TAG, "加速度传感器可以使用");
        } else {
            Log.v(TAG, "加速度传感器无法使用");
        }
    }

    private int secondTime;

    /*开始计步计时*/
    public void startUpDateStepTimeCountSecond() {
        timeOfLastPeak = timeOfThisPeak;
        timeOfThisPeak = System.currentTimeMillis();
        longArrayList.add(timeOfThisPeak);

       /* if (timeOfThisPeak - timeOfLastPeak <= 3000) {
            Log.d(TAG, " 开始计算");
            downTimer.resume();
            //mStepTimerCount.start();
            //iscomputeTime = true;
        } else {
            //mStepTimerCount.cancel();
            Log.d(TAG, " 结束计算");
            downTimer.pause();
            //iscomputeTime = false;
        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /**
     * 保存记步数据
     */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // 如果计时器正常结束，则开始计步
            time.cancel();
            save();
            startTimeCount();
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

    }

    class StepTimeCountSecond extends CountDownTimer {


        public StepTimeCountSecond(long millisinfuture, long countDownInterval) {
            super(millisinfuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.d(TAG, " steptimecountsecond  :" + stepTimeCountSecond + "");
            iscomputeTime = true;
        }

        @Override
        public void onFinish() {
            //存储数据库;
            // startStepTimeCountSecond();
            /*stepTimeCountSecond++;
            iscomputeTime = false;
            save();
            mCallback.updateUi(CURRENT_STEP, stepTimeCountSecond); //  时间结束即刻更新
            //updateNotification();
            Log.d(TAG, " steptimecountsecond   save   :" + stepTimeCountSecond + " save ");*/

        }
    }

    /**
     * 保存记步数据
     */
    private void save() {
        if (googlefitOnUpdaUi) {
            return;
        }

        int tempStep = CURRENT_STEP;
        Log.d(TAG, "save()   保存数据  更新数据库之前  步数 ：" + tempStep);

        int tempStepSecond = stepTimeCountSecond;

        String todaySimpleDate = getTodaySimpleDate();

        Date dateNow = new Date();
        int todayhour = StepConversion.getStepHour(dateNow);
        //List<StepData> list = daoSession.getStepDataDao().qu.getQueryByWhere(StepData.class, StepData.STEP_SIMPLEDATE, new String[]{todaySimpleDate});
        List<StepData> list = daoSession.getStepDataDao().queryBuilder().where(StepDataDao.Properties.StepToday.eq(todaySimpleDate)).list();
        StepData data = new StepData();
        if (list.size() == 0) {
            long count = daoSession.getStepDataDao().count();
            data.setStep(tempStep);
            data.setStepTime(dateNow);
            data.setStepSecond(tempStepSecond);
            data.setStepToday(todaySimpleDate);
            data.setId(count + 1);
            data.setReset("false");
            daoSession.insertOrReplace(data);
            //lastHourStepAll =tempStep ;
            Log.d(TAG, "save()  today List  is 0 ");
        } else {
            List<StepData> stepDataListAsc = daoSession.getStepDataDao()
                    .queryBuilder()
                    .orderAsc()
                    .list();
            //long count = daoSession.getStepDataDao().count();
            long countId = stepDataListAsc.get(stepDataListAsc.size() - 1).getId();
            Log.d(TAG, "save()   db size " + countId);
            StepData lastone = daoSession.getStepDataDao().loadByRowId(countId);

            //StepData lastone = lastoneList.get(0);
            if (StepConversion.getStepHour(lastone.getStepTime()) == todayhour) {
                lastone.setStepToday(getTodaySimpleDate());
                lastone.setStep(tempStep - (int) sharedPreferencesUtils.getParam(SharedPreferencesUtils.LAST_HOURSTEP_ALL, 0));
                lastone.setStepTime(dateNow);
                lastone.setStepSecond(tempStepSecond - (int) sharedPreferencesUtils.getParam(SharedPreferencesUtils.LAST_HOURSTEPSECOND_ALL, 0));
                lastone.setId(countId);
                lastone.setReset("false");
                daoSession.update(lastone);
                Log.d(TAG, "save()   存储的时刻 （小时）相同    开始存储  " + (lastone.getStep()));

                if (StepConversion.getStepHour(new Date(dateNow.getTime() + duration)) != todayhour) {
                    Log.d(TAG, "save:    这一时刻的最后一分钟,存储步骤： " + tempStep);
                    lastHourStepAll = tempStep;
                    lastHourStepSecondAll = tempStepSecond;
                    sharedPreferencesUtils.setParam(SharedPreferencesUtils.LAST_HOURSTEP_ALL, lastHourStepAll);
                    sharedPreferencesUtils.setParam(SharedPreferencesUtils.LAST_HOURSTEPSECOND_ALL, lastHourStepSecondAll);
                }

                if (StepConversion.getStepHour(new Date(dateNow.getTime() + duration)) != todayhour) {

                }
            } else {
                StepData stepData = new StepData();
                stepData.setStep(tempStep - (int) sharedPreferencesUtils.getParam(SharedPreferencesUtils.LAST_HOURSTEP_ALL, 0)); //换下一个小时存储；
                stepData.setStepSecond(tempStepSecond - (int) sharedPreferencesUtils.getParam(SharedPreferencesUtils.LAST_HOURSTEPSECOND_ALL, 0));
                stepData.setStepTime(dateNow);
                stepData.setStepToday(getTodaySimpleDate());
                stepData.setReset("false");
                daoSession.insert(stepData);
                //lastHourStepAll =tempStep;
                Log.d(TAG, "save()   存储的时刻 （小时）不相同    开始存储  " + stepData.getStep());
            }
        }
    }

    public void unRegisterReceiver() {

        googlefitOnUpdaUi = false;

        closeReceiverSenserChanger();

        Log.d(TAG, "关闭计步传感器接收");
    }

    private void closeReceiverSenserChanger() {
        if (mStepCount != null) {
            sensorManager.unregisterListener(mStepCount.getStepDetector());
            mStepCount.setOffSteps(false);

        }

        if (sensorManager != null) {
            sensorManager.unregisterListener(this, countSensor);
            sensorManager.unregisterListener(this, detectorSensor);
        }
    }

    public void registerReceiver() {
        googlefitOnUpdaUi = true;
        //sensorManager.registerListener(mStepCount.getStepDetector(),sensor_acceleremeter ,SensorManager.SENSOR_DELAY_UI);
        if (mStepCount != null) {
            mStepCount.setOffSteps(true);
            startStepDetector();
        }
        Log.d(TAG, "开启计步器传感器接收");
        if (sensorManager != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBusUtil.unregister(this);
        //取消前台进程
        stopForeground(true);
        DaoManager.getInstance().closeConnection();
        unregisterReceiver(mBatInfoReceiver);
        if (time != null) {
            time.cancel();
        }
        Log.d(TAG, "stepService关闭");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
