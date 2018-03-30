package com.baisi.spedometer.utiles;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;

/**
 * Created by MnyZhao on 2017/11/15.
 */

public class TouchRectUtils {
    private String TAG = "TouchRect";
    private View view;
    private int rect;

    /**
     * @param view 要改变点击区域的控件
     * @param rect 改变的范围 数值随意设
     */
    public TouchRectUtils(View view, int rect) {
        this.view = view;
        this.rect = rect;
        setViewTouchRect(view, rect);
    }


    /**
     * 改变控件的点击范围 通过TouchDelegate  设置自身的范围
     *
     * @param view 要改变点击区域的控件
     * @param rect 扩大的点击区域范围
     */
    private void setViewTouchRect(final View view, final int rect) {
        view.post(new Runnable() {
            @Override
            public void run() {
                // 构造一个 "矩型" 对象
                Rect delegateArea = new Rect();
                View delegate = view;
                // Hit rectangle in parent's coordinates
                delegate.getHitRect(delegateArea);

                // 扩大触摸区域矩阵值
                delegateArea.left -= rect;
                delegateArea.top -= rect;
                delegateArea.right += rect;
                delegateArea.bottom += rect;
                /**
                 * 构造扩大后的触摸区域对象
                 * 第一个构造参数表示  扩大范围
                 * 第二个构造参数表示 被扩大的View对象
                 */
                TouchDelegate expandedArea = new TouchDelegate(delegateArea, delegate);

                if (View.class.isInstance(delegate.getParent())) {
                    // 设置视图扩大后的触摸区域
                    ((View) delegate.getParent()).setTouchDelegate(expandedArea);
                }
            }
        });
    }
}
