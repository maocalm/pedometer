package com.baisi.spedometer.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import com.baisi.spedometer.R;

/**
 * Created by MnyZhao on 2017/11/7.
 *
 * @author MnyZhao
 */

public class RoundProgressJiBu extends View {
    /**
     * 画笔
     */
    private Paint paint;
    /**
     * 当前进度
     */
    private Float progress =0f;
    /**
     * 地层圆环的颜色
     */
    private int roundBgColor;
    /**
     * 上层进度颜色
     */
    private int roundProgressColor;
    /**
     * 最大进度
     */
    private Float max=0f;

    /**
     * 是否渐变色
     */
    private boolean isChange;
    /**
     * 开始渐变色
     */
    private int progressStartColor;
    /**
     * 结束渐变色
     */
    private int progressEndColor;
    private int[] colorArray ;
    /**
     * 圆环的宽度
     */
    private float roundWidth;
    /**
     * 进度风格实心 0 空心 1
     */
    private int style;
    public static final int STROKE = 0;
    public static final int FILL = 1;

    public RoundProgressJiBu(Context context) {
        this(context, null);
    }

    public RoundProgressJiBu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressJiBu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.reset();
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressCircle);
        /*获取自定义的属性*/
        roundBgColor = mTypedArray.getColor(R.styleable.RoundProgressCircle_roundBgColor, Color.parseColor("#342055"));
        roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressCircle_roundProgressColor, Color.BLUE);
        roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressCircle_roundWidth, 10);
        isChange = mTypedArray.getBoolean(R.styleable.RoundProgressCircle_isChange, false);
        progressStartColor = mTypedArray.getColor(R.styleable.RoundProgressCircle_roundProgressShapeStart, Color.parseColor("#914BE2"));
        progressEndColor = mTypedArray.getColor(R.styleable.RoundProgressCircle_roundProgressShapeEnd, Color.parseColor("#E32C84"));
        style = mTypedArray.getInt(R.styleable.RoundProgressCircle_style, 0);
        colorArray  = new int[]{progressStartColor, progressEndColor};
        progress=0f;
        max=1f;
        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        /*画底外层圆环*/
        /*获取圆心x坐标*/
        int centre = getWidth() / 2;
        /*圆环半径*/
        int radius = (int) (centre - roundWidth / 2);
        /*设置圆环颜色*/
        paint.reset();
        paint.setColor(roundBgColor);
        /*空心*/
        paint.setStyle(Paint.Style.STROKE);
        /*圆环宽度*/
        paint.setStrokeWidth(roundWidth);
        /*消除锯齿*/
        paint.setAntiAlias(true);
        /*画底层圆环*/
        canvas.drawCircle(centre, centre, radius, paint);
        /*起始小角度*/
        Float little  = roundWidth/(2*3.1416f*radius)*360f;

        /*画圆弧进度*/
        /*设置进度圆环宽度*/
        paint.reset();
        paint.setStrokeWidth(roundWidth);
        /*设置进度圆环风格 带圆角*/
        paint.setStrokeCap(Paint.Cap.ROUND);
        /*设置圆环进度颜色 根据是否渐变来区分*/
        if (isChange) {
            /**
             *   参数1 2 3 4 渐变色渲染端点也就是起始坐标
             *   参数5 6 起始颜色
             *   7 渲染模式 有三种 CLAMP延伸 MIRROR 镜像模式 REPEAT 重复模式
             */
            Shader shader = new LinearGradient(getWidth() / 2, 0, getWidth() / 2, getHeight(), progressStartColor, progressEndColor, Shader.TileMode.CLAMP);
            //Shader shader = new SweepGradient(getWidth() / 2, getWidth() / 2, colorArray, null);
           /* Matrix matrix = new Matrix();
            matrix.setRotate(-little-90, getWidth() / 2, getHeight() / 2);//加上旋转还是很有必要的，每次最右边总是有一部分多余了,不太美观,也可以不加
            shader.setLocalMatrix(matrix);*/
            paint.setShader(shader);
        } else {
            paint.setColor(roundProgressColor);
        }
        /*设置圆弧形状以及大小界限*/
        RectF oval = new RectF(centre - radius, centre - radius, centre
                + radius, centre + radius);
        /*设置圆弧风格*/
        switch (style) {
            case STROKE:
                paint.setStyle(Paint.Style.STROKE);
                /*根据进度画圆弧参数2 即改变起始位置*/
                if (progress != 0&& max!=0) {
                    canvas.drawArc(oval, -90, 360 * progress / max, false, paint);
                }
                break;
            case FILL:
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                if (progress != 0&&max!=0) {
                     /*根据进度画圆弧参数2 即改变起始位置*/
                    canvas.drawArc(oval, -90, 360 * progress / max, true, paint);
                }
                break;
            default:
                break;
        }
    }

    /**
     * @return 最大进度值
     */
    public synchronized Float getMax() {
        return max;
    }

    /**
     * 设置最大进度值
     *
     * @param max
     */
    public synchronized void setMax(Float max) {
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }

    /**
     * 获取进度，需要同步
     *
     * @return
     */
    public synchronized Float getProgress() {
        return progress;
    }

    /**
     * 设置进度 线程安全空间 同步刷新
     * 刷新界面调用postInvalidate()能在非UI线程刷新
     *
     * @param newProgress
     */
    public synchronized void setProgress(Float newProgress) {
        if (newProgress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (newProgress > max) {
            newProgress = max;
        }
        if (newProgress <= max) {
            this.progress = newProgress;
            //postInvalidate();
           // invalidate();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(progress, newProgress);
            valueAnimator.setDuration(300);
            valueAnimator.setInterpolator(new Interpolator() {
                @Override
                public float getInterpolation(float v) {
                    return 1-(1-v)*(1-v)*(1-v);
                }
            });
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    progress = (Float) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });
            valueAnimator.start();
        }

    }

    /**
     * 获取底层圆环的颜色
     *
     * @return
     */
    public int getRoundBgColor() {
        return roundBgColor;
    }

    /**
     * 设置底层圆环背景色
     *
     * @param roundBgColor
     */
    public void setRoundBgColor(int roundBgColor) {
        this.roundBgColor = roundBgColor;
    }

    /**
     * 获取外层圆环的颜色
     *
     * @return
     */
    public int getRoundProgressColoe() {
        return roundProgressColor;
    }

    /**
     * 设置外层圆环的颜色
     *
     * @param roundProgressColor
     */
    public void setRoundProgressColor(int roundProgressColor) {
        this.roundProgressColor = roundProgressColor;
    }

    /**
     * 设置渐变色的开始颜色
     *
     * @param progressStartColor
     */
    public void setProgressStartColor(int progressStartColor) {
        this.progressStartColor = progressStartColor;
    }
    /**
     * 获取渐变色开始颜色
     *
     * @return
     */
    public int getProgressStartColor() {
        return progressStartColor;
    }

    /**
     * 设置渐变色结束颜色
     *
     * @param progressEndColor
     */
    public void setProgressEndColor(int progressEndColor) {
        this.progressEndColor = progressEndColor;
    }

    /**
     * 获取渐变色结束颜色s
     *
     * @return
     */
    public int getProgressEndColor() {
        return progressEndColor;
    }

    /**
     * 设置圆环宽度
     *
     * @param roundWidth
     */
    public void setRoundWidth(float roundWidth) {
        this.roundWidth = roundWidth;
    }

    /**
     * 获取圆环宽度
     *
     * @return
     */
    public float getRoundWidth() {
        return roundWidth;
    }

}
