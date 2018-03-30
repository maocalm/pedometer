package com.baisi.spedometer.step.accelerometer;


import android.util.Log;


/*
* 根据StepDetector传入的步点"数"步子
* */
public class StepCount implements StepCountListener {

    private String TAG = "stepcount";
    private int count = 0;
    private int mCount = 0;
    private StepValuePassListener mStepValuePassListener;
    private long timeOfLastPeak = 0;
    private long timeOfThisPeak = 0;
    private StepDetector stepDetector;

    /*开启计步开关*/
    private boolean onOff =true;

    public StepCount() {
        stepDetector = new StepDetector();
        stepDetector.initListener(this);
    }

    public StepDetector getStepDetector() {
        return stepDetector;
    }

    /*
        * 连续走十步才会开始计步
        * 连续走了9步以下,停留超过3秒,则计数清空
        * */
    @Override
    public void countStep() {
        if (!onOff){
            return;
        }
        this.timeOfLastPeak = this.timeOfThisPeak;
        this.timeOfThisPeak = System.currentTimeMillis();
        if (this.timeOfThisPeak - this.timeOfLastPeak <= 3000L) {
            if (this.count < 9) {
                Log.d(TAG, "countStep:  count <9");
                this.count++;
            } else if (this.count == 9) {

                Log.d(TAG, "countStep:  count <9");
                this.count++;
                //this.mCount = 0;
                //notifyListener();
            } else {
                Log.d(TAG, "countStep:  count >9");
                this.mCount++;
                notifyListener();
            }
        } else {//超时
            this.count = 1;//为1,不是0
            Log.d(TAG, "停留超时(三秒)");
        }

    }

    public void initListener(StepValuePassListener listener) {
        this.mStepValuePassListener = listener;
    }

    public void notifyListener() {
        if (this.mStepValuePassListener != null)
            this.mStepValuePassListener.stepChanged(this.mCount);
    }

    public void setSteps(int initValue) { // 更新ui
        this.mCount = initValue;
        this.count = 0;
        timeOfLastPeak = 0;
        timeOfThisPeak = 0;
        notifyListener();
    }

    public void setOffSteps(boolean on_off) {
        if (on_off) {
            onOff = true;
        } else {
            onOff = false;
        }
    }
}
