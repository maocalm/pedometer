package com.fashare.timer_view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Created by jinliangshan on 17/1/11.
 * 计数代理 View, 接入 ITimer 的逻辑实现.
 * UI 展示由子类自行实现.
 *
 * @see DigitalTimerView
 */
public abstract class BaseTimerView extends LinearLayout implements ITimer, ITimer.OnCountTimeListener {

    private String TAG = getClass().getSimpleName();
    private ITimer mTimer = new ITimer.CountDown();
    private OnCountTimeListener mOnCountTimeListener;

    public void setTimer(ITimer timer) {
        mTimer = timer;
    }

    public BaseTimerView(Context context) {
        super(context);
        init();
    }

    public BaseTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseTimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        mTimer.setOnCountTimeListener(this);
    }

    @Override
    final public void setLifeCycleListener(LifeCycleListener lifeCycleListener) {
        mTimer.setLifeCycleListener(lifeCycleListener);
    }

    @Override
    final public void setOnCountTimeListener(OnCountTimeListener onCountTimeListener) {
        mOnCountTimeListener = onCountTimeListener;
    }

    @Override
    final public void start(long initTime) {
        mTimer.start(initTime);
    }

    @Override
    final public void stop() {
        mTimer.stop();
        Log.d(TAG, "stop: ");
    }

    @Override
    final public void pause() {
        mTimer.pause();
    }

    @Override
    final public void pauseToStart() {
        mTimer.pauseToStart();
    }

    @Override
    final public void countTime(long curTime) {
        updateShow(curTime);

        if (mOnCountTimeListener != null)
            mOnCountTimeListener.countTime(curTime);
    }

    @Override
    public void onEnd() {
    }

    /**
     * 由子类决定如何更新 UI
     *
     * @param curTime
     */
    public abstract void updateShow(long curTime);
}
