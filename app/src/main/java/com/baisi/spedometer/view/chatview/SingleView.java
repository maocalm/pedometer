package com.baisi.spedometer.view.chatview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.nfc.Tag;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.baisi.spedometer.R;
import com.baisi.spedometer.step.utils.SharedPreferencesUtils;
import com.baisi.spedometer.utiles.DensityUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SingleView extends View {

    private Paint mPaint, mChartPaint, mCirclePaint, mGoalRectPaint, mlineGoalPaint, mTextGoalPaint, mTextStepPaint, paintLineBottome;
    private Rect mBound;
    private int mStartWidth, mChartWidth, mSize;
    private int mWidth, mHeight;
    private int lineColor, leftColor, lefrColorBottom, selectLeftColor;
    private List<Float> list = new ArrayList<>();
    private getNumberListener listener;
    private int number = 1000;
    private int selectIndex = -1;
    private List<Integer> selectIndexRoles = new ArrayList<>();


    private List<String> list_bottom = new ArrayList<>();

    private int goal;
    private boolean isPaintStep;   //选中画step
    private float size;

    private String TAG = "singleview";
    private float mManyPointsSize;
    private String classification;
    private DensityUtil densityUtil;
    private float chartWith ;

    public void setList(List<Float> list, String classification) {
        this.list = list;
        this.classification = classification;
        mSize = getWidth() / 15;
        mStartWidth = getWidth() / 8 + 20;
         mChartWidth = getWidth() / 8 - mSize / 2;
         //mChartWidth = densityUtil.dip2px(8);
        invalidate();
    }

    public void setGoal(int goal) {
        this.goal = goal;
        //setSize(goal);
    }

    public int getGoal() {
        return goal;
    }

    public void setChartPaintColor(int color) {
        mChartPaint.setColor(color);
        lefrColorBottom = color;
    }


    public void setSize(String classification) {
        SharedPreferencesUtils mSharedPreferencesUtils = new SharedPreferencesUtils(getContext());
        String goalStirng = mSharedPreferencesUtils
                .getParam(SharedPreferencesUtils.STEP_GOAL, SharedPreferencesUtils.STEPGOAL_DEFAULT)
                .toString();
        goal = Integer.valueOf(goalStirng);
        ArrayList<Float> arrayList = new ArrayList();
        arrayList.addAll(this.list);
        float max = Collections.max(arrayList);
        if (classification.equals("step")) {

            if (max >= goal) {
                mManyPointsSize = goal + 500;
                //mManyPointsSize = Float.floatToIntBits(max)+50;
            } else {
                mManyPointsSize = goal;
            }

        } else if (classification.equals("calorie")) {
            mManyPointsSize = max + 50;
            Log.d(TAG, "setSize --- calorie ---max " + max);
            Log.d(TAG, "setSize --- calorie ---mManyPointsSize " + mManyPointsSize);

        } else if (classification.equals("time")) {
            mManyPointsSize = max + 5;

        } else if (classification.equals("distance")) {
            mManyPointsSize = max + 1;

        }

        // size  = arrayList.get(6)+50 ;
        this.size = (mHeight - 100f - 15f) / mManyPointsSize;
    }


    public SingleView(Context context) {
        this(context, null);
        densityUtil = new DensityUtil(context);
        mChartWidth = densityUtil.dip2px(8);
    }

    public SingleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MyChartView, defStyleAttr, 0);
        int n = array.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.MyChartView_xyColor:
                    lineColor = array.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.MyChartView_leftColor:
                    // 默认颜色设置为黑色
                    leftColor = array.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.MyChartView_leftColorBottom:
                    lefrColorBottom = array.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.MyChartView_selectLeftColor:
                    // 默认颜色设置为黑色
                    selectLeftColor = array.getColor(attr, Color.BLACK);
                    break;
                default:
                    bringToFront();
            }
        }
        array.recycle();
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width;
        int height;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = widthSize * 1 / 2;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = heightSize * 1 / 2;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG, "onLayout ");
        mWidth = getWidth();
        mHeight = getHeight();

        mStartWidth = getWidth() / 8 + 20;
        mSize = (mWidth) / 15;
         mChartWidth = getWidth() / 8 - mSize / 2;
        //mChartWidth = densityUtil.dip2px(8);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mBound = new Rect();


        mChartPaint = new Paint();
        mChartPaint.setAntiAlias(true);

        paintLineBottome = new Paint();
        paintLineBottome.setColor(Color.GRAY);
        paintLineBottome.setStrokeWidth(5);


        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(getResources().getColor(R.color.report_tv_stroke));
        mCirclePaint.setStrokeWidth(3);


        mlineGoalPaint = new Paint();
        mlineGoalPaint.setAntiAlias(true);
        mlineGoalPaint.setColor(Color.GRAY);

        mTextGoalPaint = new Paint();
        mTextGoalPaint.setAntiAlias(true);
        mTextGoalPaint.setColor(Color.WHITE);

        mTextStepPaint = new Paint();
        mTextStepPaint.setAntiAlias(true);
        mTextStepPaint.setColor(Color.BLACK);

        mGoalRectPaint = new Paint();
        mGoalRectPaint.setAntiAlias(true);
        mGoalRectPaint.setColor(getResources().getColor(R.color.report_tv_stroke));
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        Log.d(TAG, "onWindowVisibilityChanged");
        if (visibility == VISIBLE) {
            mSize = (getWidth()) / 15;
            mStartWidth = getWidth() / 8 + 20;
            mChartWidth = getWidth() / 8 - mSize / 2;
            //mChartWidth = densityUtil.dip2px(8);
        }
        list_bottom.add("S");
        list_bottom.add("M");
        list_bottom.add("T");
        list_bottom.add("W");
        list_bottom.add("T");
        list_bottom.add("F");
        list_bottom.add("S");
        list_bottom.add("S");
        list_bottom.add("S");
        list_bottom.add("S");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(lineColor);


        for (int i = 0; i < 7; i++) {

            if (selectIndexRoles.contains(i)) {
                //画数字
            /*2.1 合适对齐*/
                mPaint.setTextSize(35);
                mPaint.setTextAlign(Paint.Align.CENTER);
                mPaint.getTextBounds(list_bottom.get(i), 0, 1, mBound);
                canvas.drawText(list_bottom.get(i), mStartWidth + i * (mChartWidth * 2.1f / 4),
                        mHeight - 60 + mBound.height() * 1 / 2, mPaint);


                /*选中画圆*/
                float textWith = mPaint.measureText(list_bottom.get(i));
                float textHeight = mBound.bottom - mBound.top;
                float circleHeight = mHeight - 60;
                canvas.drawCircle(mStartWidth + i * (mChartWidth * 2.1f / 4), circleHeight, 50 / 2, mCirclePaint);
                mStartWidth += getWidth() / 13; //

//                mPaint.getFontMetrics().bottom
                //mPaint.getFontMetrics()
                float wight = mPaint.measureText(list_bottom.get(i));
                float height = mPaint.measureText(list_bottom.get(i));
                //canvas.drawCircle(wight / 2, height / 2, wight / 2, mPaint);

            } else {
                //画数字
            /*2.1 合适对齐*/
                mPaint.setTextSize(35);
                mPaint.setTextAlign(Paint.Align.CENTER);
                mPaint.getTextBounds(String.valueOf(i + 1) + "", 0, String.valueOf(i).length(), mBound);
                canvas.drawText(list_bottom.get(i), mStartWidth + i * (mChartWidth * 2.1f / 4),
                        mHeight - 60 + mBound.height() * 1 / 2, mPaint);
                mStartWidth += getWidth() / 13;
            }

        }

        // 减去基线高度, 15 是三角的
        // size = (mHeight - 100f - 15f) / 100f;
        // setSize(classification);
        for (int i = 0; i < 7; i++) {
            mChartPaint.setStyle(Paint.Style.FILL);
            if (list.size() > 0) {
                if (selectIndexRoles.contains(i)) {
                    mChartPaint.setShader(null);
                    mChartPaint.setColor(selectLeftColor);

                    /*画已经走的步骤控制*/
                    isPaintStep = true;

                } else {
                    isPaintStep = false;
                    LinearGradient lg = new LinearGradient(mChartWidth, mChartWidth + mSize, mHeight - 100,
                            (float) (mHeight - 100 - list.get(i) * size), lefrColorBottom, lefrColorBottom, Shader.TileMode.MIRROR);
                    mChartPaint.setShader(lg);
                    //LinearGradient lg = new LinearGradient(mChartWidth, mHeight - 100, mChartWidth + mSize, mHeight - 100 - list.get(i), (float) (mHeight - 100 - list.get(i) * size), lefrColorBottom, lefrColorBottom, Shader.TileMode.MIRROR);
                    //mChartPaint.setShader(lg);
                    //mCirclePaint.setColor(leftColor);
                }

                //画柱状图
                RectF rectF = new RectF();
                rectF.left = mChartWidth + 20 + 5;
                rectF.right = mChartWidth + mSize + 20 - 5;
                rectF.bottom = mHeight - 100;  // 基线横轴
                if (list.get(i) * size > mHeight - 100) { // 控制超出屏幕不在绘制
                    rectF.top = 0;
                } else {
                    rectF.top = mHeight - 100 - (list.get(i) * size);  //柱状图的高度；
                }
                //rectF.top = mHeight - 100 - (list.get(i) * size);  //柱状图的高度；
                canvas.drawRoundRect(rectF, 50, 50, mChartPaint);

                /*画 step  text  */
                RectF rectF1 = new RectF(mChartWidth - 20 + 20, 30, (int) rectF.right + 20, 80);
                Log.d(TAG, String.valueOf(rectF1.centerX()) + "  X");
                Log.d(TAG, String.valueOf(rectF1.centerY()) + "  Y");
                if (isPaintStep) {
                    mTextStepPaint.setTextAlign(Paint.Align.CENTER);
                    mTextStepPaint.setTextSize(30);
                    Paint.FontMetricsInt fontMetrics = mTextStepPaint.getFontMetricsInt();
                    float baseline = (rectF1.top + rectF1.bottom - fontMetrics.bottom - fontMetrics.top) / 2;
                    canvas.drawRoundRect(rectF1, 10, 10, mChartPaint);
                    String x = String.valueOf(list.get(i));
                    if (classification.equals("step")) {
                        x = String.format("%.0f", list.get(i));
                    }
                    canvas.drawText(x, rectF1.centerX(), baseline, mTextStepPaint); //居中


                    /*画 step text 下的指示线 */
                    canvas.drawLine(rectF1.centerX(), rectF1.centerY() + rectF1.height() / 2, rectF1.centerX(), rectF.top, mChartPaint);
                }

                mChartWidth += getWidth() / 8; //步进

            }


        }
        /*画底部基线*/
        canvas.drawLine(30, mHeight - 100, mWidth - 30, mHeight - 100, paintLineBottome);


        // 画goal箭头 静态；
      /*  RectF rectF1 = new RectF(0, 0, 80, 30);
        canvas.drawRect(rectF1, mlineGoalPaint);
        Path path = new Path();
        path.moveTo(80, mHeight - 100 - mHeight + 130); //下
        path.lineTo(80 + 30, mHeight - 100 - mHeight + 130 - 15); // 箭头点
        path.lineTo(80, mHeight - 100 - mHeight + 130 - 30); // 上
        path.close();
        canvas.drawPath(path, mlineGoalPaint);*/

       /* // 画goal箭头线 静态；
        canvas.drawLine(80 + 30, mHeight - 100 - mHeight + 130 - 15, mWidth,
                mHeight - 100 - mHeight + 130 - 15, mlineGoalPaint);*/

        // 画画goal箭头的的字： 静态
        /*goal = 7000;
        String goalText = String.valueOf(goal);
        Rect rectGoalText = new Rect(0, 0, 80, 30);

        mTextGoalPaint.getTextBounds(goalText, 0, goalText.length(), rectGoalText);
        mTextGoalPaint.setTextAlign(Paint.Align.CENTER);
        mTextGoalPaint.setTextSize(20);
        canvas.drawText(goalText, 40, 24, mTextGoalPaint);
        */

        // goal 箭头动态的 ,矩形高度为30 ，长度 为 80；
        float rectfY = this.size * goal;
        Log.d(TAG, "size : " + size + "goal : " + goal);
        RectF rectF = new RectF(0, mHeight - 100f - (rectfY + 15), 50, mHeight - 100f - (rectfY - 15));
        canvas.drawRect(rectF, mGoalRectPaint);
        Path path = new Path();
        path.moveTo(50, mHeight - 100f - (rectfY - 15)); //下
        path.lineTo(50 + 15, mHeight - 100f - rectfY); // 箭头点
        path.lineTo(50, mHeight - 100f - (rectfY + 15)); // 上
        path.close();
        canvas.drawPath(path, mGoalRectPaint);

        /*动态的画 goal 线 */
         /* canvas.drawLine(50 + 15, mHeight - 100f - rectfY, mWidth,
                mHeight - 100f - rectfY, mlineGoalPaint);*/

        Path pathLine = new Path();

        pathLine.moveTo(50 + 15, mHeight - 100f - rectfY);
        pathLine.lineTo(mWidth, mHeight - 100f - rectfY);
        pathLine.addCircle(-2, 0, 2, Path.Direction.CW);
        mlineGoalPaint.setPathEffect(new PathDashPathEffect(pathLine, 10, 0, PathDashPathEffect.Style.TRANSLATE));
        canvas.drawPath(pathLine, mlineGoalPaint);

        // 画画goal箭头的的字：动态
        String goalText = String.valueOf(goal);
        mTextGoalPaint.setTextAlign(Paint.Align.CENTER);
        mTextGoalPaint.setTextSize(20);
        Paint.FontMetricsInt fontMetrics = mTextGoalPaint.getFontMetricsInt();
        float baseline = (rectF.top + rectF.bottom - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(goalText, rectF.centerX(), baseline, mTextGoalPaint); //居中

        // 画左刻度值；
        if (!classification.equals("step")) {
            mTextGoalPaint.setTextAlign(Paint.Align.LEFT);
            String keduTopString = String.valueOf(mManyPointsSize);
            String keduHalfString = String.valueOf(mManyPointsSize / 2);
            RectF rectFK = new RectF(0, 0, 50, 30);
            float baselineK = (rectFK.top + rectFK.bottom - fontMetrics.bottom - fontMetrics.top) / 2;
            canvas.drawText(keduTopString, rectFK.centerX() - 10, baselineK, mTextGoalPaint);
            RectF rectFKHalf = new RectF(0, (mHeight - 100) / 2 - 15, 50, (mHeight - 100) / 2 - 15);
            float baselineKHalf = (rectFKHalf.top + rectFKHalf.bottom - fontMetrics.bottom - fontMetrics.top) / 2;
            canvas.drawText(keduHalfString, rectFKHalf.centerX() - 10, baselineKHalf, mTextGoalPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int x = (int) ev.getX();
        int y = (int) ev.getY();
        int left = 0;
        int top = 0;
        int right = mWidth / 7;
        int bottom = mHeight - 10;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < 7; i++) {
                    Rect rect = new Rect(left, top, right, bottom);
                    left += mWidth / 7;
                    right += mWidth / 7;
                    if (rect.contains(x, y)) {
                        if (listener != null) {
                            //listener.getNumber(i, x, y);
                            number = i;
                            selectIndex = i;
                            selectIndexRoles.clear();
                            selectIndexRoles.add(selectIndex);

                            mSize = (getWidth()) / 15;
                            mStartWidth = getWidth() / 8 + 20;
                            mChartWidth = getWidth() / 8 - mSize / 2;
                            //mChartWidth = densityUtil.dip2px(8);
                            invalidate();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }

    public void setListener(getNumberListener listener) {
        this.listener = listener;
    }

    public interface getNumberListener {
        void getNumber(int number, int x, int y);
    }
}
