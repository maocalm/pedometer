package com.baisi.spedometer.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.baisi.spedometer.R;


public class TextViewDrawable extends AppCompatTextView {

    private int drawableLeftWidth, drawableTopWidth, drawableRightWidth, drawableBottomWidth;
    private int drawableLeftHeight, drawableTopHeight, drawableRightHeight, drawableBottomHeight;
    private boolean isAliganCenter=true;
    private boolean isDwMath_content=false;
    private int mWidth, mHeight;

    public TextViewDrawable(Context context) {
        this(context, null);
    }

    public TextViewDrawable(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextViewDrawable(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextViewDrawableControl);
        drawableLeftWidth = typedArray.getDimensionPixelSize(R.styleable.TextViewDrawableControl_drawableLeftWidth, 0);
        drawableTopWidth = typedArray.getDimensionPixelSize(R.styleable.TextViewDrawableControl_drawableTopWidth, 0);
        drawableRightWidth = typedArray.getDimensionPixelSize(R.styleable.TextViewDrawableControl_drawableRightWidth, 0);
        drawableBottomWidth = typedArray.getDimensionPixelSize(R.styleable.TextViewDrawableControl_drawableBottomWidth, 0);
        drawableLeftHeight = typedArray.getDimensionPixelSize(R.styleable.TextViewDrawableControl_drawableLeftHeight, 0);
        drawableTopHeight = typedArray.getDimensionPixelSize(R.styleable.TextViewDrawableControl_drawableTopHeight, 0);
        drawableRightHeight = typedArray.getDimensionPixelSize(R.styleable.TextViewDrawableControl_drawableRightHeight, 0);
        drawableBottomHeight = typedArray.getDimensionPixelSize(R.styleable.TextViewDrawableControl_drawableBottomHeight, 0);
        isAliganCenter = typedArray.getBoolean(R.styleable.TextViewDrawableControl_isAliganCenter, true);
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    private int getMySize(int defaultSize, int measureSpec) {
        int mySize = defaultSize;

        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.UNSPECIFIED: {//如果没有指定大小，就设置为默认大小
                mySize = defaultSize;
                break;
            }
            case MeasureSpec.AT_MOST: {//如果测量模式是最大取值为size
                //我们将大小取最大值,你也可以取其他值
                mySize = 2*drawableTopHeight-24;
                break;
            }
            case MeasureSpec.EXACTLY: {//如果是固定的大小，那就不要去改变它
                mySize = size;
                break;
            }
        }
        return mySize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMySize(40, widthMeasureSpec);
        int height = getMySize(40, heightMeasureSpec);

        /*if (width < height) {
            height = width;
        } else {
            width = height;
        }*/

        setMeasuredDimension(width, height);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        Drawable[] drawables = getCompoundDrawables();
        Drawable drawableLeft = drawables[0];
        Drawable drawableTop = drawables[1];
        Drawable drawableRight = drawables[2];
        Drawable drawableBottom = drawables[3];
        if (drawableLeft != null) {
            setDrawable(drawableLeft, 0, drawableLeftWidth, drawableLeftHeight);
        }
        if (drawableTop != null) {
            setDrawable(drawableTop, 1, drawableTopWidth, drawableTopHeight);
        }
        if (drawableRight != null) {
            setDrawable(drawableRight, 2, drawableRightWidth, drawableRightHeight);
        }
        if (drawableBottom != null) {
            setDrawable(drawableBottom, 3, drawableBottomWidth, drawableBottomHeight);
        }
        this.setCompoundDrawables(drawableLeft,drawableTop,drawableRight,drawableBottom);
    }

    private void setDrawable(Drawable drawable, int tag, int drawableWidth, int drawableHeight) {
        //获取图片实际长宽
        int width = drawableWidth == 0 ? drawable.getIntrinsicWidth() : drawableWidth;
        int height = drawableHeight == 0 ? drawable.getIntrinsicHeight() : drawableHeight;
        int left = 0, top = 0, right = 0, bottom = 0;
        switch (tag) {
            case 0:
            case 2:
                left = 0;
                top = isAliganCenter ? 0 : -getLineCount() * getLineHeight() / 2 + getLineHeight() / 2;
                right = width;
                bottom = top + height;
                break;
            case 1:
                left =isAliganCenter ? 0: -mWidth/2+width/2;
                top = 0;
                right = left+width;
                bottom =top+height;
                break;
        }
        drawable.setBounds(left, top, right, bottom);
    }
}