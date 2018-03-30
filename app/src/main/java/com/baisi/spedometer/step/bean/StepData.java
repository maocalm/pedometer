package com.baisi.spedometer.step.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;

import org.greenrobot.greendao.annotation.Generated;

/**
 * 表
 */


@Entity
public class StepData {
    public static final String STEP = "step";
    public static final String STEPTIME = "stepTime";
    public static final String STEPSECOND = "stepSecond";
    public static final String STEP_SIMPLEDATE = "stepToday";
    public static final String STEP_ISRESET = "step_reset";
    // 指定自增，每个对象需要有一个主键

    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private int step;
    @NotNull
    private int stepSecond;
    @NotNull
    private Date stepTime;
    @NotNull
    private String stepToday;
    @NotNull
    private String reset;

    @Generated(hash = 110278849)
    public StepData(Long id, int step, int stepSecond, @NotNull Date stepTime,
                    @NotNull String stepToday, @NotNull String reset) {
        this.id = id;
        this.step = step;
        this.stepSecond = stepSecond;
        this.stepTime = stepTime;
        this.stepToday = stepToday;
        this.reset = reset;
    }

    @Generated(hash = 90761876)
    public StepData() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getStep() {
        return this.step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getStepSecond() {
        return this.stepSecond;
    }

    public void setStepSecond(int stepSecond) {
        this.stepSecond = stepSecond;
    }

    public Date getStepTime() {
        return this.stepTime;
    }


    public void setStepTime(Date stepTime) {
        this.stepTime = stepTime;
    }

    public String getStepToday() {
        return this.stepToday;
    }

    public void setStepToday(String stepToday) {
        this.stepToday = stepToday;
    }


    public String getReset() {
        return this.reset;
    }

    public void setReset(String reset) {
        this.reset = reset;
    }

    @Override
    public String toString() {
        return "StepData{" +
                "id=" + id +
                ", step=" + step +
                ", stepSecond=" + stepSecond +
                ", stepTime=" + stepTime +
                ", stepToday='" + stepToday + '\'' +
                '}';
    }
}
