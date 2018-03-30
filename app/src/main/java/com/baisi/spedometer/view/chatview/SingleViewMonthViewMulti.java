package com.baisi.spedometer.view.chatview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.baisi.spedometer.R;
import com.baisi.spedometer.step.utils.SharedPreferencesUtils;
import com.baisi.spedometer.utiles.DensityUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by hanwenmao on 2018/1/1.
 */

public class SingleViewMonthViewMulti extends View {

    private Paint mPaint, mChartPaint, mCirclePaint, mGoalRectPaint, mlineGoalPaint, mTextGoalPaint, mTextStepPaint, paintLineBottome;
    private Rect mBound;
    private int mStartWidth;// 开始画柱状图的位置；
    private int mChartWidth; // 柱状图的宽度；
    private int mSize; //
    private int mWidth, mHeight;
    private int lineColor, leftColor, lefrColorBottom, selectLeftColor;
    private List<Float> list = new ArrayList<>();
    private SingleView.getNumberListener listener;
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
    private float chartWith;
    private int gapidth;
    private float baseLineHeight;
    private int indexTextheight;
    private int diTextSize;
    private float goalLength;
    private float goalWith;
    private int goalTextSize;
    private int stepTextFrameWidth, stepTextFrameHeight;
    private int stepTextSize;
    private int stepTextFrameLocation;

    private static final String STEP_STR = "step", CALORIE_STR = "calorie", TIME_STR = "time", DISTANCE_STR = "distance";

    // singview 是today  还是 month，weekReport;
    private String singViewType;

    public void setList(List<Float> list, String classification) {
        this.list = list;
        this.classification = classification;
        initStart();
        init();
        invalidate();
    }

    private void initStart() {
        //mSize = getWidth() / 15;
        //mStartWidth = getWidth() / 8 + 20;
        //mChartWidth = getWidth() / 8 - mSize / 2;

        mChartWidth = densityUtil.dip2px(16);
        mStartWidth = densityUtil.dip2px(60);
        Log.d(TAG, "mstartWidth  :" + mStartWidth);
        Log.d(TAG, "mChartWidth  :" + mChartWidth);
        mSize = (getWidth() - mStartWidth) / 14;

        // 右边空白出来的位置
        int lengthToRight = densityUtil.dip2px(20);

        //柱状图间隙
        gapidth = ((getWidth() - mStartWidth) - lengthToRight - 7 * mChartWidth) / 6;

        // baseLine  Height
        baseLineHeight = getHeight() - densityUtil.dip2px(50);
        indexTextheight = getHeight() - densityUtil.dip2px(30);

        // 日期字体大小 ；
        diTextSize = densityUtil.dip2px(15);

        // goal 箭头得长度，高度；
        goalLength = densityUtil.dip2px(30);
        goalWith = densityUtil.dip2px(15);

        // goal 字体的大小；
        goalTextSize = densityUtil.dip2px(10);

        //y 轴刻度字体大小；
        int scaleYTextSize = densityUtil.dip2px(13);


        // 点击显示步骤的框的宽度,高度 ,字体大小 ；
        stepTextFrameWidth = densityUtil.dip2px(50);
        stepTextFrameHeight = densityUtil.dip2px(25);
        stepTextSize = densityUtil.dip2px(15);
        // 点击显示步骤弹框的位置；
        stepTextFrameLocation = 50;

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
        initStart();
        SharedPreferencesUtils mSharedPreferencesUtils = new SharedPreferencesUtils(getContext());
        String goalStirng = mSharedPreferencesUtils
                .getParam(SharedPreferencesUtils.STEP_GOAL, SharedPreferencesUtils.STEPGOAL_DEFAULT)
                .toString();
        goal = Integer.valueOf(goalStirng);
        ArrayList<Float> arrayList = new ArrayList();
        arrayList.addAll(this.list);
        float max = 0;
        if (arrayList.size() != 0) {
            max = Collections.max(arrayList);
        }
        Random random =new Random();
        if (classification.equals(STEP_STR)) {

            if (max >= goal) {
                mManyPointsSize = goal + 500;
                //mManyPointsSize = Float.floatToIntBits(max)+50;
            } else {
                mManyPointsSize = goal;
            }

        } else if (classification.equals(CALORIE_STR)) {
            //mManyPointsSize = max + max+random.nextFloat()*max;
            mManyPointsSize = max + 50;
            Log.d(TAG, "setSize --- calorie ---max " + max);
            Log.d(TAG, "setSize --- calorie ---mManyPointsSize " + mManyPointsSize);

        } else if (classification.equals(TIME_STR)) {
            //mManyPointsSize = max + max-random.nextFloat()*max+5;
            mManyPointsSize = max +5;

        } else if (classification.equals(DISTANCE_STR)) {
            //mManyPointsSize = max + random.nextFloat()*max;
            mManyPointsSize = max + 1;

        }

        // size  = arrayList.get(6)+50 ;
        this.size = (baseLineHeight - densityUtil.dip2px(14)) / mManyPointsSize;
    }

    public SingleViewMonthViewMulti(Context context) {
        this(context, null);
        Log.d(TAG, "构造，SingView2(Context context)  ");
        densityUtil = new DensityUtil(context);
        mChartWidth = densityUtil.dip2px(8);
    }

    public SingleViewMonthViewMulti(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        Log.d(TAG, "构造，SingView2(Context context ,AttributeSet attrs)  ");
        densityUtil = new DensityUtil(context);
        mChartWidth = densityUtil.dip2px(8);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Singview2);
        singViewType = typedArray.getString(R.styleable.Singview2_type_kind);

    }

    public SingleViewMonthViewMulti(Context context, AttributeSet attrs, int defStyleAttr) {
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
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == View.MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = widthSize * 1 / 2;
        }
        if (heightMode == View.MeasureSpec.EXACTLY) {
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

        initStart();
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
        mCirclePaint.setStrokeWidth(5);


        mlineGoalPaint = new Paint();
        mlineGoalPaint.setAntiAlias(true);
        mlineGoalPaint.setColor(Color.GRAY);

        mTextGoalPaint = new Paint();
        mTextGoalPaint.setAntiAlias(true);
        mTextGoalPaint.setColor(Color.WHITE);
        mTextGoalPaint.setTextSize(goalTextSize);

        mTextStepPaint = new Paint();
        mTextStepPaint.setAntiAlias(true);
        mTextStepPaint.setColor(Color.WHITE);
        mTextStepPaint.setTextSize(stepTextSize);

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
            initStart();
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

    public static Bitmap drawableToBitmap(Drawable drawable, float width, float height) {

        //int w = drawable.getIntrinsicWidth();
        //BigDecimal b = new BigDecimal( result );
        //Float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        int w = Math.round(width);
        int h = Math.round(height);

        if (w <=0 ){
            w =1;
        }
        if (h<=0){
            h=1;
        }
        System.out.println("Drawable转Bitmap");
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        //注意，下面三行代码要用到，否则在View或者SurfaceView里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (classification == null) {
            return;
        }
        initStart();
        mPaint.setColor(lineColor);
        RectF rectFIndex = null;

        if (singViewType.equals("today")) {


        } else if (singViewType.equals("week")) {

            //画下刻度；
            for (int i = 0; i < 7; i++) {

                if (selectIndexRoles.contains(i)) {

                    mPaint.setTextSize(diTextSize);
                    mPaint.setTextAlign(Paint.Align.CENTER);
                    rectFIndex = new RectF(mStartWidth, indexTextheight, mStartWidth + mChartWidth, indexTextheight + mChartWidth);
                    //canvas.drawRect(rectFIndex, mGoalRectPaint);
                    Paint.FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
                    float indexBasLine = (rectFIndex.top + rectFIndex.bottom - fontMetricsInt.bottom - fontMetricsInt.top) / 2;
                    canvas.drawText(list_bottom.get(i), rectFIndex.centerX(), indexBasLine, mPaint);
                    canvas.drawCircle(rectFIndex.centerX(), rectFIndex.centerY(), rectFIndex.width(), mCirclePaint);

                } else {

                    mPaint.setTextSize(diTextSize);
                    mPaint.setTextAlign(Paint.Align.CENTER);
                    rectFIndex = new RectF(mStartWidth, indexTextheight, mStartWidth + mChartWidth, indexTextheight + mChartWidth);
                    //canvas.drawRect(rectFIndex, mGoalRectPaint);
                    Paint.FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
                    float indexBasLine = (rectFIndex.top + rectFIndex.bottom - fontMetricsInt.bottom - fontMetricsInt.top) / 2;
                    canvas.drawText(list_bottom.get(i), rectFIndex.centerX(), indexBasLine, mPaint);
                    // TODO: 2017/12/30  获取当天的日期；
                    if (6==(i+1)){
                        canvas.drawCircle(rectFIndex.centerX(), rectFIndex.centerY(),rectFIndex.width() , mCirclePaint);
                    }
                }

                mStartWidth = mStartWidth + gapidth + mChartWidth;

            }
        } else if (singViewType.equals("month")) {

        }


        // 减去基线高度, 15 是三角的
        // size = (mHeight - 100f - 15f) / 100f;
        if (classification != null) {
            setSize(classification);
        }
        for (int i = 0; i < list.size(); i++) {
            mChartPaint.setStyle(Paint.Style.FILL);
            if (list.size() > 0) {
                if (selectIndexRoles.contains(i)) {
                    mChartPaint.setShader(null);
                    mChartPaint.setColor(selectLeftColor);

                    //画已经走的步骤控制
                    isPaintStep = true;

                } else {
                    isPaintStep = false;
                    LinearGradient lg = new LinearGradient(mChartWidth, mChartWidth + mSize, baseLineHeight,
                            (float) (baseLineHeight - list.get(i) * size), lefrColorBottom, lefrColorBottom, Shader.TileMode.MIRROR);
                    mChartPaint.setShader(lg);
                    //LinearGradient lg = new LinearGradient(mChartWidth, mHeight - 100, mChartWidth + mSize, mHeight - 100 - list.get(i), (float) (mHeight - 100 - list.get(i) * size), lefrColorBottom, lefrColorBottom, Shader.TileMode.MIRROR);
                    //mChartPaint.setShader(lg);
                    //mCirclePaint.setColor(leftColor);
                }

                //画柱状图c
                RectF rectF = new RectF(mStartWidth, indexTextheight, mStartWidth + mChartWidth, indexTextheight + mChartWidth);

                RectF rectFHistogram = new RectF();
                rectFHistogram.left = rectF.left;
                rectFHistogram.right = rectF.right;
                rectFHistogram.bottom = baseLineHeight;

                if (list.get(i) * size > baseLineHeight) { // 控制超出屏幕不在绘制
                    rectFHistogram.top = 0;
                } else {
                    rectFHistogram.top = baseLineHeight - (list.get(i) * size);  //柱状图的高度；
                    Log.d(TAG, " zhu zhuang tu top  " + rectFHistogram.top);
                    Log.d(TAG, " zhu zhuang tu left   " + rectFHistogram.left);
                    Log.d(TAG, " zhu zhuang tu right  " + rectFHistogram.right);
                    Log.d(TAG, " zhu zhuang tu bottom  " + rectFHistogram.bottom);
                }
                //rectF.top = mHeight - 100 - (list.get(i) * size);  //柱状图的高度；
                //canvas.drawRoundRect(rectFHistogram, 30, 30, mChartPaint);

                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setCornerRadii(new float[]{50, 50, 50, 50, 0, 0, 0, 0});
                if (!isPaintStep) {
                    gradientDrawable.setColor(lefrColorBottom);
                } else {
                    gradientDrawable.setColor(selectLeftColor);
                }
                if (Math.round(rectFHistogram.width()) != 0 && Math.round(rectFHistogram.height()) != 0) {  // 精度缩小后如果为0 就不画了；
                    Bitmap bitmap = drawableToBitmap(gradientDrawable, rectFHistogram.width(), rectFHistogram.height());
                    canvas.drawBitmap(bitmap, null, rectFHistogram, mCirclePaint);
                }


                //画 step  text
                RectF rectFStepText = new RectF(rectFHistogram.centerX() - stepTextFrameWidth / 2, stepTextFrameLocation, rectFHistogram.centerX() + stepTextFrameWidth / 2, stepTextFrameHeight + 30);
                if (isPaintStep) {
                    mTextStepPaint.setTextAlign(Paint.Align.CENTER);
                    // mTextStepPaint.setTextSize(30);
                    Paint.FontMetricsInt fontMetrics = mTextStepPaint.getFontMetricsInt();
                    float baseline = (rectFStepText.top + rectFStepText.bottom - fontMetrics.bottom - fontMetrics.top) / 2;
                    canvas.drawRoundRect(rectFStepText, 10, 10, mChartPaint);
                    String x = String.valueOf(list.get(i));
                    //if (classification.equals("step")) {
                    x = String.format("%.2f", list.get(i));
                    if (classification.equals(STEP_STR)) {
                        x = String.format("%.0f", list.get(i));
                    }
                    //}
                    canvas.drawText(x, rectFStepText.centerX(), baseline, mTextStepPaint); //居中


                    //画 step text 下的指示线
                    canvas.drawLine(rectFStepText.centerX(), rectFStepText.centerY() + rectFStepText.height() / 2, rectFStepText.centerX(), rectFHistogram.top, mChartPaint);
                }


            }
            mStartWidth = mStartWidth + gapidth + mChartWidth;

        }
        /*画底部基线*/
        canvas.drawLine(30, baseLineHeight, mWidth - 30, baseLineHeight, paintLineBottome);

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
        RectF rectF = new RectF(0f, baseLineHeight - (rectfY + goalWith / 2f), goalLength, baseLineHeight - (rectfY - goalWith / 2f));
        canvas.drawRect(rectF, mGoalRectPaint);
        Path path = new Path();
        path.moveTo(goalLength, baseLineHeight - (rectfY - goalWith / 2f)); //下
        path.lineTo(goalLength + goalWith / 2f, baseLineHeight - rectfY); // 箭头点
        path.lineTo(goalLength, baseLineHeight - (rectfY + goalWith / 2f)); // 上
        path.close();
        canvas.drawPath(path, mGoalRectPaint);

       /*动态的画 goal 线 */
        Path pathLine = new Path();
        pathLine.moveTo(goalLength + goalWith / 2f, baseLineHeight - rectfY);
        pathLine.lineTo(mWidth, baseLineHeight - rectfY);
        pathLine.addCircle(-2f, 0f, 2f, Path.Direction.CW);
        mlineGoalPaint.setPathEffect(new PathDashPathEffect(pathLine, 10, 0, PathDashPathEffect.Style.TRANSLATE));
        canvas.drawPath(pathLine, mlineGoalPaint);

        // 画画goal箭头的的字：动态
        String goalText = String.valueOf(goal);
        mTextGoalPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetricsInt fontMetrics = mTextGoalPaint.getFontMetricsInt();
        float baseline = (rectF.top + rectF.bottom - fontMetrics.bottom - fontMetrics.top) / 2f;
        canvas.drawText(goalText, rectF.centerX(), baseline, mTextGoalPaint); //居中

        // 画左刻度值；
        if (!classification.equals(STEP_STR)) {
            mTextGoalPaint.setTextAlign(Paint.Align.LEFT);


            RectF rectFK = new RectF(0, 0, goalLength, goalWith / 2);
            float baselineK = (rectFK.top + rectFK.bottom - fontMetrics.bottom - fontMetrics.top) / 2;
            BigDecimal b = new BigDecimal(mManyPointsSize);
            Float result = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            String scalTop = String.valueOf(result);
            canvas.drawText(scalTop, rectFK.centerX(), baselineK, mTextGoalPaint);


            RectF rectFKHalf = new RectF(0, (baseLineHeight) / 2 - goalWith / 2, goalLength, (baseLineHeight) / 2 - goalWith / 2);
            float baselineKHalf = (rectFKHalf.top + rectFKHalf.bottom - fontMetrics.bottom - fontMetrics.top) / 2;
            BigDecimal b1 = new BigDecimal(mManyPointsSize / 2);
            Float result2 = b1.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            String keduHalfString = String.valueOf(result2);
            canvas.drawText(keduHalfString, rectFKHalf.centerX(), baselineKHalf, mTextGoalPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int x = (int) ev.getX();
        int y = (int) ev.getY();
        initStart();
        float left = mStartWidth - gapidth / 2;
        int top = 0;
        float right = left + gapidth + mChartWidth;
        int bottom = mHeight - 10;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < 7; i++) {
                    RectF rect = new RectF(left, top, right, bottom);
                    left += gapidth + mChartWidth;
                    right += gapidth + mChartWidth;
                    if (rect.contains(x, y)) {
                        //listener.getNumber(i, x, y);
                        number = i;
                        selectIndex = i;
                        selectIndexRoles.clear();
                        selectIndexRoles.add(selectIndex);
                        initStart();
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }

    public void setListener(SingleView.getNumberListener listener) {
        this.listener = listener;
    }

    public interface getNumberListener {
        void getNumber(int number, int x, int y);
    }
}
