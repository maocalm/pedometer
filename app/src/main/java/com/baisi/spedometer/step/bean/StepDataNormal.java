package com.baisi.spedometer.step.bean;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.Date;

/**
 * Created by hanwenmao on 2018/1/3.
 */

public class StepDataNormal {

    private int step;

    private int stepSecond;

    private Date stepTime;

    public StepDataNormal(int step, int stepSecond, Date stepTime, String stepToday, boolean isTopTitle) {
        this.step = step;
        this.stepSecond = stepSecond;
        this.stepTime = stepTime;
        this.stepToday = stepToday;
        this.isTopTitle = isTopTitle;
    }

    public StepDataNormal() {
    }

    private String stepToday;

    private boolean isTopTitle;


    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public boolean getIsTopTitle() {
        return isTopTitle;
    }

    public void setIsTopTitle(boolean isTopTitle) {
        this.isTopTitle = isTopTitle;
    }

    public int getStepSecond() {
        return stepSecond;

    }

    public void setStepSecond(int stepSecond) {
        this.stepSecond = stepSecond;
    }

    public Date getStepTime() {
        return stepTime;
    }

    public void setStepTime(Date stepTime) {
        this.stepTime = stepTime;
    }

    public String getStepToday() {
        return stepToday;
    }

    public void setStepToday(String stepToday) {
        this.stepToday = stepToday;
    }

    @Override
    public String toString() {
        return "StepDataNormal{" +
                "step=" + step +
                ", stepSecond=" + stepSecond +
                ", stepTime=" + stepTime +
                ", stepToday='" + stepToday + '\'' +
                ", isTopTitle='" + isTopTitle + '\'' +
                '}';
    }
}
