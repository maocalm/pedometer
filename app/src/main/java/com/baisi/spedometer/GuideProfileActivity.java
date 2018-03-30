package com.baisi.spedometer;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baisi.spedometer.step.utils.SharedPreferencesUtils;
import com.baisi.spedometer.utiles.DensityUtil;
import com.baisi.spedometer.utiles.Firebase;
import com.baisi.spedometer.view.numberpicker.NumberPicker;
import com.facebook.share.Share;

public class GuideProfileActivity extends AppCompatActivity implements View.OnClickListener {


    private LinearLayout parent_height;
    private LinearLayout parent_weight;
    private TextView weight_tv;
    private TextView height_tv;
    private Button tv_next;
    private PopupWindow popupWindow;
    private NumberPicker numberPicker;
    private SharedPreferencesUtils sharedPreferencesUtils;
    private int height;
    private String weight;
    private NumberPicker weight_number_pick_right;
    private NumberPicker weight_number_pick_left;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_profile);
        initView();
    }

    private void initView() {
        parent_height = (LinearLayout) findViewById(R.id.parent_height);
        parent_weight = (LinearLayout) findViewById(R.id.parent_weight);
        weight_tv = (TextView) findViewById(R.id.weight);
        height_tv = (TextView) findViewById(R.id.height);
        tv_next = (Button) findViewById(R.id.tv_next);

        parent_height.setOnClickListener(this);
        parent_weight.setOnClickListener(this);
        tv_next.setOnClickListener(this);

        sharedPreferencesUtils = new SharedPreferencesUtils(this);
        height = (int)sharedPreferencesUtils.getParam(SharedPreferencesUtils.HEIGHT, SharedPreferencesUtils.Height_DEFAULT);
        weight = sharedPreferencesUtils.getParam(SharedPreferencesUtils.WEIGHT, SharedPreferencesUtils.WEIGHT_DEFAULT).toString();
        sharedPreferencesUtils.getParam(SharedPreferencesUtils.WEIGHT, SharedPreferencesUtils.WEIGHT_DEFAULT);
        height_tv.setText(String.valueOf(height));
        weight_tv.setText(weight);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.parent_height:
                popWindowHeight();
                break;
            case R.id.parent_weight:
                popWindoWeight();
                break;
            case R.id.tv_next:
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                sharedPreferencesUtils.setParam(SharedPreferencesUtils.FIRST_COMEIN, false);
                startActivity(intent);
                break;
            case R.id.height_ok:
                int heightValue = numberPicker.getValue();
                height_tv.setText(String.valueOf(heightValue));
                sharedPreferencesUtils.setParam(SharedPreferencesUtils.HEIGHT, heightValue);
                Firebase.getInstance(getApplicationContext()).logEvent("引导页","身高设置",String.valueOf(heightValue));
                popupWindow.dismiss();
                break;
            case R.id.height_cancle:
                popupWindow.dismiss();
                break;
            case R.id.ok:
                int weightValueLeft = weight_number_pick_left.getValue();
                int weightValueRight = weight_number_pick_right.getValue();
                String  value = weightValueLeft+"."+weightValueRight ;
                weight_tv.setText(String.valueOf(value));
                sharedPreferencesUtils.setParam(SharedPreferencesUtils.WEIGHT, Float.valueOf(value));
                Firebase.getInstance(getApplicationContext()).logEvent("引导页","体重设置",String.valueOf(value));
                popupWindow.dismiss();
                break;
            case R.id.cancle:
                popupWindow.dismiss();
                break;

        }

    }

    private void initPopWindow(View view) {
        DensityUtil  densityUtil = new DensityUtil(this) ;
        popupWindow = new PopupWindow(view,
               densityUtil.dip2px(320), densityUtil.dip2px(300));

        // 设置背景透明
        WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        layoutParams.alpha = 0.5f;
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(layoutParams);

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        View rootview = LinearLayout.inflate(this, R.layout.activity_main, null);
        popupWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow()
                        .getAttributes();
                lp.alpha = 1f;
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                getWindow().setAttributes(lp);
            }
        });
    }

    private void popWindowHeight() {
        View view = LinearLayout.inflate(this, R.layout.popwindow_heght, null);
        initPopWindow(view);
        numberPicker = (NumberPicker) view.findViewById(R.id.number_pick);
        numberPicker.setMinValue(90);
        numberPicker.setMaxValue(240);
        numberPicker.setValue((Integer) sharedPreferencesUtils.getParam(SharedPreferencesUtils.HEIGHT ,SharedPreferencesUtils.Height_DEFAULT));
        numberPicker.setSelectedTextSize(R.dimen.number_picker_size);
        numberPicker.setTextSize(R.dimen.number_picker_size);
        numberPicker.setDividerThickness(1);
        numberPicker.setDividerColor(getResources().getColor(R.color.set_text_color));
        numberPicker.setOnValueChangedListener(new com.baisi.spedometer.view.numberpicker.NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(com.baisi.spedometer.view.numberpicker.NumberPicker picker, int oldVal, int newVal) {
                //滚动完成的操作；
            }
        });

        view.findViewById(R.id.height_ok).setOnClickListener(this);
        view.findViewById(R.id.height_cancle).setOnClickListener(this);
    }

    private void popWindoWeight() {
        View view = LinearLayout.inflate(this, R.layout.pop_weight, null);
        initPopWindow(view);

        weight_number_pick_right = (NumberPicker) view.findViewById(R.id.number_pick_right);
        weight_number_pick_left = (NumberPicker) view.findViewById(R.id.number_pick_left);
        weight_number_pick_left.setMinValue(20);
        weight_number_pick_left.setMaxValue(200);
        weight_number_pick_left.setSelectedTextSize(R.dimen.number_picker_size);
        weight_number_pick_left.setTextSize(R.dimen.number_picker_size);
        weight_number_pick_left.setDividerThickness(1);


        weight_number_pick_right.setMinValue(0);
        weight_number_pick_right.setMaxValue(9);
        weight_number_pick_right.setSelectedTextSize(R.dimen.number_picker_size);
        weight_number_pick_right.setTextSize(R.dimen.number_picker_size);
        weight_number_pick_right.setDividerThickness(1);

        String weight =  sharedPreferencesUtils.getParam(SharedPreferencesUtils.WEIGHT ,SharedPreferencesUtils.WEIGHT_DEFAULT).toString();
        weight_number_pick_left.setValue(Integer.valueOf(weight.substring(0, weight.indexOf("."))));
        weight_number_pick_right.setValue(Integer.valueOf(weight.substring(weight.indexOf(".") + 1)));
        //weight_number_pick_left.setDividerColor(getResources().getColor(R.color.set_text_color));
        //weight_number_pick_right.setDividerColor(getResources().getColor(R.color.set_text_color));


        view.findViewById(R.id.ok).setOnClickListener(this);
        view.findViewById(R.id.cancle).setOnClickListener(this);
    }
}
