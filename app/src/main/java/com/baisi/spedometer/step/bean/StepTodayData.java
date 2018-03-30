package com.baisi.spedometer.step.bean;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by hanwenmao on 2017/12/27.
 */

@Entity
public class StepTodayData {
    @Id
    private Long id;

    private String name;

    @Transient
    private int tempUsageCount; // not persisted

    @Generated(hash = 1864855677)
    public StepTodayData(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Generated(hash = 1421586482)
    public StepTodayData() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
