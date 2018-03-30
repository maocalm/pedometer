package com.baisi.spedometer.step;

/**
 * 步数更新回调
 *
 */
public interface UpdateUiCallBack {
    /**
     * 更新UI步数
     *
     * @param stepCount 步数
     * @param stepTimeCountSecond  时长
     */
    void updateUi(int stepCount ,int stepTimeCountSecond );
}
