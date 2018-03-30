package com.baisi.spedometer.step.utils;

import android.content.Context;

import com.baisi.spedometer.step.bean.StepData;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 单位换算
 * Created by hanwenmao on 2017/11/27.
 */

public class StepConversion {
    private static String gender;
    private static int height;
    private static float weight;

    public StepConversion(Context context) {

    }

    public static void initConversion(Context context) {
        SharedPreferencesUtils mSpUtils = new SharedPreferencesUtils(context);
        weight = (Float) mSpUtils.getParam(SharedPreferencesUtils.WEIGHT, SharedPreferencesUtils.WEIGHT_DEFAULT);
        gender = (String) mSpUtils.getParam(SharedPreferencesUtils.GENDER, SharedPreferencesUtils.GENDER_DEFAULT);
        height = (int) mSpUtils.getParam(SharedPreferencesUtils.HEIGHT, SharedPreferencesUtils.Height_DEFAULT);
    }

    /**
     * from  step get calorie
     * 保留两位小数
     *
     * @param mWeight
     * @param mStep
     * @return Kcal
     */
    public static String getCalorie(float mWeight, int mStep) {

        Float value = (mWeight / 2000) * mStep;
        String resulet = String.format("%.2f", value);
        return resulet;
    }

    /**
     * BSL=SEXP*HT
     * basic stride length=Sex parameter*Height
     * 步幅= 性别参数*身高
     * <p>
     * male = 0.85
     * femal = 0.8
     *
     * @param gender
     * @param mStep
     * @param mHeight 厘米
     * @return km
     */
    public static String getDistance(String gender, int mStep, int mHeight) {

        float sexParameter, value = 0;
        if (gender != null) {
            if (gender.equals("female")) {
                sexParameter = 0.8f;
            } else {
                sexParameter = 0.85f;
            }
            value = mStep * sexParameter * mHeight / 100000;

        }
        String result = String.format("%.2f", value);
        return result;

    }


    /**
     * @param stepList
     * @param context
     * @return
     */
    public static ArrayList getCalorieList(List<Float> stepList, Context context) {
        initConversion(context);
        ArrayList arrayList = new ArrayList();
        if (stepList == null) {
            return arrayList;
        }
        for (int i = 0; i < stepList.size(); i++) {
            float result = (weight / 2000) * stepList.get(i);
            BigDecimal b = new BigDecimal(result);
            Float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            arrayList.add(f1);
        }
        return arrayList;
    }

    public static ArrayList getTimeList(Context context) {
        initConversion(context);
        //模拟数据 ；
        ArrayList arrayList = new ArrayList();
        arrayList.add(1f);
        arrayList.add(2f);
        arrayList.add(4f);
        arrayList.add(5f);
        arrayList.add(9f);
        arrayList.add(3f);
        arrayList.add(1f);


       /* ArrayList arrayList =new ArrayList();
        for (int  i =0 ;i <timeList.size() ; i++ ){
            float result  =
        }*/
        return arrayList;

    }

    public static ArrayList getDistanceList(List<Float> stepList, Context context) {
        initConversion(context);
        ArrayList arrayList = new ArrayList();
        float sexParameter, value = 0;

        if (stepList == null) {
            return arrayList;
        }
        for (int i = 0; i < stepList.size(); i++) {
            if (gender != null) {
                if (gender.equals("female")) {
                    sexParameter = 0.8f;
                } else {
                    sexParameter = 0.85f;
                }
                value = stepList.get(i) * sexParameter * height / 100000;
                BigDecimal b = new BigDecimal(value);
                Float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                arrayList.add(f1);
            }
        }
        return arrayList;
    }

    /**
     * 求和
     *
     * @param stepList float
     * @return
     */
    public static Float getTotalFloatSumList(List<Float> stepList) {

        Float sumSteps = 0f;
        if (stepList != null) {

            for (float step : stepList) {
                sumSteps += step;
            }
        }
        return sumSteps;
    }

    public static float getTotalStepSumList(List<Float> stepList) {

        float sumSteps = 0f;
        if (stepList != null) {
            for (float step : stepList) {
                sumSteps += step;
            }
        }
        return sumSteps;
    }

    /**获取当天的  “ 周一  -   周日”
     * @return 英文
     */
    public static String getTodayWeekRange(Date date) {

        SimpleDateFormat simdf = new SimpleDateFormat("MMM DD", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            c.add(Calendar.DAY_OF_MONTH, -1);
        }
        c.add(Calendar.DATE, c.getFirstDayOfWeek() - c.get(Calendar.DAY_OF_WEEK) + 1);
        String mondayString = simdf.format(c.getTime());


        Calendar c2 = Calendar.getInstance();
        c.setTime(date);
        String sundayString = null;
        // 如果是周日直接返回
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            sundayString = simdf.format(date);
        }

        c.add(Calendar.DATE, 7 - c2.get(Calendar.DAY_OF_WEEK) + 1);
        sundayString = simdf.format(c.getTime());

        String result = mondayString + " - " + sundayString;
        return result;
    }

    /**
     * h获取date的周一到周日的日期；
     *
     * @param time date
     * @return
     */
    public static List<Date> getDateWeekList(Date time) {

        ArrayList<Date> weekList = new ArrayList();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()); //设置时间格式
        Calendar cal = Calendar.getInstance();
        cal.setTime(time); //判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);//获得传入日期是一个星期的第几天
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        System.out.println("要计算日期为:" + sdf.format(cal.getTime())); //输出要计算日期
        cal.setFirstDayOfWeek(Calendar.SUNDAY);//设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        int day = cal.get(Calendar.DAY_OF_WEEK);//获得传入日期是一个星期的第几天
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);//根据日历的规则，给传入日期减去星期几与一个星期第一天的差值
        String Monday = sdf.format(cal.getTime());
        Date MondayDate = cal.getTime();
        weekList.add(MondayDate);

        System.out.println("所在周星期一的日期：" + Monday);
        cal.add(Calendar.DATE, 1);
        String Tuesday = sdf.format(cal.getTime());
        Date TuesdayDate = cal.getTime();
        weekList.add(TuesdayDate);

        System.out.println("所在周星期二的日期：" + Tuesday);
        cal.add(Calendar.DATE, 1);
        String Wednesday = sdf.format(cal.getTime());
        Date WednesdayDate = cal.getTime();
        weekList.add(WednesdayDate);

        System.out.println("所在周星期三的日期：" + Wednesday);
        cal.add(Calendar.DATE, 1);
        String Thursday = sdf.format(cal.getTime());
        Date ThursdayDate = cal.getTime();
        weekList.add(ThursdayDate);

        System.out.println("所在周星期四的日期：" + Thursday);
        cal.add(Calendar.DATE, 1);
        String Friday = sdf.format(cal.getTime());
        Date FridayDate = cal.getTime();
        weekList.add(FridayDate);

        System.out.println("所在周星期五的日期：" + Friday);
        cal.add(Calendar.DATE, 1);
        String Saturday = sdf.format(cal.getTime());
        Date SaturdayDate = cal.getTime();
        weekList.add(SaturdayDate);

        System.out.println("所在周星期六的日期：" + Saturday);
        cal.add(Calendar.DATE, 1);
        String Sunday = sdf.format(cal.getTime());
        Date SundayDate = cal.getTime();
        weekList.add(SundayDate);

        System.out.println("所在周星期日的日期：" + Sunday);

        return weekList;
    }

    /**
     * 获取今天是一周中那一天
     *
     * @param date
     * @return
     */
    public static int getDayofweek(Date date) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (date.equals("")) {
            cal.setTime(new Date(System.currentTimeMillis()));
        } else {
            cal.setTime(date);
        }
        return cal.get(Calendar.DAY_OF_WEEK);
    }


    /**
     * 获取date  在几点 ，24小时
     *
     * @param date
     * @return
     */
    public static int getStepHour(Date date) {
        int index = 0;
        DateFormat dateFormat = new SimpleDateFormat("HH", Locale.getDefault());
        index = Integer.valueOf(dateFormat.format(date));
//        Calendar calendar =Calendar.getInstance();
//        calendar.setTime(new Date());
//        index = calendar.get(Calendar.HOUR_OF_DAY);
        return index;
    }

    /**
     * 获取月份
     * @param date  MMM
     * @return   String MMM
     */
    public static String getMonth(Date date) {
        String month;
        DateFormat dateFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        month = dateFormat.format(date);;
        return month ;
    }



    /**
     * 获取月份
     * @param date
     * @return   String MM
     */
    public static String getMonth_MM(Date date) {
        String month;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        month = dateFormat.format(date);;
        return month ;
    }

    /**
     * 获取传入日期是这个月的第几天；
     * @param date
     * @return  int
     */
    public static int getTodayInt (Date date ){
        int day  ;
        DateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
        day = Integer.valueOf(dateFormat.format(date));
        return  day ;
    }


    /**
     * 获取当月的 天数
     * */
    public static int getCurrentMonthDay() {

        Calendar a = Calendar.getInstance();
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 根据年 月 获取对应的月份 天数
     * */
    public static int getDaysByYearMonth(int year, int month) {

        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 获取当天日期 yyyy-mm-dd
     *
     * @return
     */
    public static String getTodaySimpleDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * 获取当天日期 mm-dd
     *
     * @return
     */
    public static String getTodaySimple2Date(Date  date) {
        //Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * 获取当天日期 MMM
     *
     * @return
     */
    public static String getTodaySimpleDate_MMM() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * 获取参数日期 yyyy-mm-dd
     *
     * @param date Date 类型
     * @return
     */
    public static String getSimpleDate(Date date) {
        //Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     *
     * @param dateString     yyyy-MM-dd
     * @return
     */
    public static Date getSimpleDateFromString(String dateString ){
        Date date1 =null  ;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
           date1 =  sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date1 ;
    }




    /**
     * <pre>
     * 判断date和两个日期是否在同一周内
     * 注:
     * Calendar类提供了一个获取日期在所属年份中是第几周的方法，对于上一年末的某一天
     * 和新年初的某一天在同一周内也一样可以处理，例如2012-12-31和2013-01-01虽然在
     * 不同的年份中，但是使用此方法依然判断二者属于同一周内
     * </pre>
     *
     * @param date
     * @return
     */
    public static boolean isSameWeekWithToday(Date date , Date date1) {

        if (date == null) {
            return false;
        }

        // 0.先把Date类型的对象转换Calendar类型的对象
        Calendar todayCal = Calendar.getInstance();
        Calendar dateCal = Calendar.getInstance();

        todayCal.setTime(date1);
        dateCal.setTime(date);

        // 1.比较当前日期在年份中的周数是否相同
        if (todayCal.get(Calendar.WEEK_OF_YEAR) == dateCal.get(Calendar.WEEK_OF_YEAR)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 获取传入date 的零点时刻 ；
     * @param date
     * @return
     */
    public  static Long getZero(Date date) {
        Long current =date.getTime() ;
        Long zero = current/(1000*3600*24)*(1000*3600*24) - TimeZone.getDefault().getRawOffset();
        Timestamp timestamp = new Timestamp(zero);
        return  timestamp.getTime();
    }
}
