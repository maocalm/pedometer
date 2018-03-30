package com.baisi.spedometer.backfragment;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

/**
 * Created by hanwenmao on 2017/12/27.
 */

public class BackHandledFragment  extends Fragment implements FragmentBackHandler {
    @Override
    public boolean onBackPressed() {
        return interceptBackPressed()
            || (getBackHandleViewPager() == null
            ? BackHandlerHelper.handleBackPress(this)
            : BackHandlerHelper.handleBackPress(getBackHandleViewPager()));
    }


    public boolean interceptBackPressed() {
        return false;
    }


    /**
     * 2.1 版本已经不在需要单独对ViewPager处理
     *
     * @deprecated
     */
    @Deprecated
    public ViewPager getBackHandleViewPager() {
        return null;
    }
}
