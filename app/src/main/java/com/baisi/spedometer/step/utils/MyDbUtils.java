package com.baisi.spedometer.step.utils;

import android.content.Context;
import android.util.Log;


import com.baisi.spedometer.greendao.gen.StepDataDao;
import com.baisi.spedometer.step.bean.StepData;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;


public class MyDbUtils {

    private static final String TAG = MyDbUtils.class.getSimpleName();
    private DaoManager mManager;
    public static final String DB_NAME = "SpedometerStepCount";

    public MyDbUtils(Context context) {
        mManager = DaoManager.getInstance();
        mManager.init(context);


    }

    public DaoManager getDaoManager() {
        //if (mManager!=null){
        return mManager;
        //}
    }

    /**
     * 完成StepData记录的插入，如果表未创建，先创建StepData表
     *
     * @param stepData
     * @return
     */
    public boolean insertStepdata(StepData stepData) {
        boolean flag = false;
        flag = mManager.getDaoSession().getStepDataDao().insert(stepData) == -1 ? false : true;
        Log.i(TAG, "insert StepData :" + flag + "-->" + stepData.toString());
        return flag;
    }

    /**
     * 插入多条数据，在子线程操作
     *
     * @param stepData
     * @return
     */
    public boolean insertMultStepdata(final List<StepData> stepData) {
        boolean flag = false;
        try {
            mManager.getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for (StepData stepData : stepData) {
                        mManager.getDaoSession().insertOrReplace(stepData);
                    }
                }
            });
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 修改一条数据
     *
     * @param stepData
     * @return
     */
    public boolean updateStepdata(StepData stepData) {
        boolean flag = false;
        try {
            mManager.getDaoSession().update(stepData);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除单条记录
     *
     * @param stepData
     * @return
     */
    public boolean deleteStepData(StepData stepData) {
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().delete(stepData);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除所有记录
     *
     * @return
     */
    public boolean deleteAll() {
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().deleteAll(StepData.class);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 查询所有记录
     *
     * @return
     */
    public List<StepData> queryAllStepData() {
        return mManager.getDaoSession().loadAll(StepData.class);
    }

    /**
     * 根据主键id查询记录
     *
     * @param key
     * @return
     */
    public StepData queryStepDataById(long key) {
        return mManager.getDaoSession().load(StepData.class, key);
    }

    /**
     * 使用native sql进行查询操作
     */
    public List<StepData> queryStepDataByNativeSql(String sql, String[] conditions) {
        return mManager.getDaoSession().queryRaw(StepData.class, sql, conditions);
    }

    /**
     * 使用queryBuilder进行查询
     *
     * @return
     */
    public List<StepData> queryStepDataByQueryBuilder(long id) {
        QueryBuilder<StepData> queryBuilder = mManager.getDaoSession().queryBuilder(StepData.class);
        return queryBuilder.where(StepDataDao.Properties.Id.eq(id)).list();
    }

}
