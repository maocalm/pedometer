package com.baisi.spedometer.evenbus;

import org.greenrobot.eventbus.EventBus;

public class EventBusUtil {

    public static void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }

    public static void sendEvent(Event event) {
        EventBus.getDefault().post(event);
    }

    public static void sendStickyEvent(Event event) {
        EventBus.getDefault().postSticky(event);
    }

    public static class EventCode {

        public static final int STOPNOTIFICATION = 10;      // 设置界面向servi 发送 停止通知的消息 ；
        public static final int STARTNOTIFICATION = 20;     // 设置界面向service发送的开始通知栏的消息；
        public static final int RESTSTEPDATA = 30;          // 主页清空数据 发送给service 重新计算步骤 ；
        public static final int GOWEEKACTIVITY = 40;        // ped页面的点击跳转weekreportActivity ;
        public static final int FLOATACTIONBUTTON_INIT = 50;//  主页floatactionbutton  控制float在切换页面的时候是否显示；

        public static final int SPORTMODESTART = 60; // sportactivi 发往service
        public static final int SPORTMODEPAUSE = 61; //
        public static final int SPORTMODESTOP = 62; //
        public static final int SPORTMODE_SERVICE_TO_SPORTACTIVITY  = 63; //
    }
}
