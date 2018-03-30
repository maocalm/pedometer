package com.baisi.spedometer.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baisi.spedometer.R;

/**
 * Created by hanwenmao on 2018/1/9.
 */

public class MixLinearLayout extends LinearLayout {
    private ImageView imageView = null;
    private TextView tx_value = null;
    private TextView tx_unit = null;
    public MixLinearLayout(Context context) {
        this(context, null);
    }

    public MixLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public MixLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.mix_linearlayout, this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MixLinearLayout);
        initTyped(typedArray);
    }


    private void initTyped(TypedArray typedArray) {

        tx_value = (TextView) findViewById(R.id.value);
        String text = typedArray.getString(R.styleable.MixLinearLayout_textvalue_text);
        int textColor = typedArray.getColor(R.styleable.MixLinearLayout_textvalue_color, 0xffffffff);
        float textSize =  typedArray.getDimension(R.styleable.MixLinearLayout_textvalue_size, 15);
        tx_value.setTextColor(textColor);
        tx_value.setTextSize(TypedValue.COMPLEX_UNIT_PX ,textSize);
        tx_value.setText(text);



        tx_unit = (TextView) findViewById(R.id.unit);
        String textunit = typedArray.getString(R.styleable.MixLinearLayout_textunit_text);
        int textunitColor = typedArray.getColor(R.styleable.MixLinearLayout_textunit_color, 0xffffffff);
        float textUnitSize =  typedArray.getDimension(R.styleable.MixLinearLayout_textunit_size, 15);
        tx_unit.setText(textunit);
        tx_unit.setTextColor(textunitColor);
        tx_unit.setTextSize(TypedValue.COMPLEX_UNIT_PX ,textUnitSize);



        imageView = (ImageView) findViewById(R.id.imageView);
        int imageSrc = typedArray.getResourceId(R.styleable.MixLinearLayout_centerimage_src, 0);//从属性集得到图片
        int imageBg = typedArray.getResourceId(R.styleable.MixLinearLayout_centerimage_bg, 0);//从属性集得到背景图
        int imageWidth = (int) typedArray.getDimension(R.styleable.MixLinearLayout_centerimage_widh, 25);
        int imageHeight = (int) typedArray.getDimension(R.styleable.MixLinearLayout_centerimage_height, 25);
        int imageAlpha = typedArray.getInt(R.styleable.MixLinearLayout_centerimage_alpha, 255);
        imageView.setImageResource(imageSrc);
        imageView.setBackgroundResource(imageBg);
        imageView.setAlpha(imageAlpha);
        LayoutParams layoutParams = new LayoutParams(imageWidth, imageHeight);
        imageView.setLayoutParams(layoutParams);//设置图片高度
        typedArray.recycle();
    }



    //设置图片资源
    @SuppressWarnings("unused")
    private void setImgResource(int resId){
        imageView.setImageResource(resId);
    }


    //设置控件背景图片
    @SuppressWarnings("unused")
    private void setBgImage(int resId){
        imageView.setImageResource(resId);
    }

    //设置图片的高度和宽度
    private void setImageSize(int width,int height){
        LayoutParams layoutParams = new LayoutParams(width,height);
        imageView.setLayoutParams(layoutParams);
    }

    //设置图片的不透明度
    private void setImageAlpha(int alpha){
        imageView.setAlpha(alpha);
    }

    //设置文字内容
    private void setText(String text){
        tx_value.setText(text);
    }


    //设置文字颜色
    private void setTextColor(int colorValue){
        tx_value.setTextColor(colorValue);
    }

    //设置文字大小
    private void setTextSize(int size){
        tx_value.setTextSize(size);
    }
    //设置图片点击事件
    public void onMixLinearLayoutClick(final MixLinearLayoutListener mixTextImageListener){
        this.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mixTextImageListener.mClick();
            }
        });
    }
    //回调接口
    public interface MixLinearLayoutListener{
        public void mClick();
    }


}
