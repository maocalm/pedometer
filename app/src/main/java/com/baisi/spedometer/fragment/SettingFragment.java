package com.baisi.spedometer.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baisi.spedometer.MainActivity;
import com.baisi.spedometer.R;
import com.baisi.spedometer.base.BaseFragment;
import com.baisi.spedometer.evenbus.Event;
import com.baisi.spedometer.evenbus.EventBusUtil;
import com.baisi.spedometer.step.utils.SharedPreferencesUtils;
import com.baisi.spedometer.utiles.Firebase;
import com.baisi.spedometer.view.CustomPopWindow;
import com.baisi.spedometer.view.numberpicker.NumberPicker;
import com.bestgo.adsplugin.ads.activity.ShowAdFilter;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by hanwenmao on 2017/11/21.
 */

public class SettingFragment extends BaseFragment implements View.OnClickListener , ShowAdFilter {

    private View view;
    private TextView tv_show_gender;
    private TextView tv_show_height;
    private TextView tv_show_weight;
    private TextView tv_show_metric;

    private RelativeLayout rl_gender;
    private CustomPopWindow popWindow;

    private View view_gender;
    private View view_height;
    private View pop_weight;
    private View pop_metric;
    private View pop_stepgoal;

    private TextView tv_show_gender_male;
    private TextView tv_show_gender_femal;
    private TextView height_cancle;
    private TextView height_ok;
    private NumberPicker numberPicker;

    private NumberPicker weight_number_pick_right;
    private NumberPicker weight_number_pick_left;
    private TextView weight_cancle;
    private TextView weight_ok;

    private StringBuilder kgbuilder;  //

    private String mWeight_Decimal;
    private String mWeight_Integer;
    private TextView tv_show_metric_kgcm;
    private TextView tv_show_metric_lbsft;

    private SharedPreferencesUtils mSpUtils;
    private TextView tv_show_goal;
    private NumberPicker stepgoal_number_pick;
    private TextView stepgoal_ok;
    private TextView stepgoal_cancle;
    private SwitchCompat switchCompat;
    private String[] goalList = new String[]{"500", "1000", "2000", "3000", "4000", "5000", "6000"
            , "7000", "8000", "9000", "10000", "11000", "12000", "13000", "14000"
            , "15000", "16000", "17000", "18000", "19000"};


    private String TAG = "settingfragment";

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.setting_fragment, null);


        initRequired();
        initView();
        return view;
    }

    @Override
    protected void initData() {
    }

    private void initRequired() {
        mSpUtils = new SharedPreferencesUtils(getActivity());
        // mSpUtils.setParam(SharedPreferencesUtils.STEP_GOAL, 200);
    }

    /*
    * 和pedometerFragment 传递数据
    * */
    public void setPedometerFragmentStepGoalTV() {
        String stepgoalTv = mSpUtils.getParam(SharedPreferencesUtils.STEP_GOAL, SharedPreferencesUtils.STEPGOAL_DEFAULT).toString();
        tv_show_goal.setText(stepgoalTv);
        MainActivity mainActivity = (MainActivity) getActivity();
        PedometerFragment pedometerFragment = (PedometerFragment) mainActivity.getPedometerFragment();
        pedometerFragment.setStepGoalTextView(stepgoalTv);
    }

    private void initView() {
        view.findViewById(R.id.rl_gender).setOnClickListener(this);
        view.findViewById(R.id.rl_height).setOnClickListener(this);
        view.findViewById(R.id.rl_weight).setOnClickListener(this);
        view.findViewById(R.id.rl_metric).setOnClickListener(this);
        view.findViewById(R.id.rl_setp_goal).setOnClickListener(this);

        tv_show_gender = (TextView) view.findViewById(R.id.tv_show_gender);
        tv_show_height = (TextView) view.findViewById(R.id.tv_show_height);
        tv_show_weight = (TextView) view.findViewById(R.id.tv_show_weight);
        tv_show_metric = (TextView) view.findViewById(R.id.tv_show_metric);
        tv_show_goal = (TextView) view.findViewById(R.id.tv_show_goal);
        switchCompat = (SwitchCompat) view.findViewById(R.id.swc);

        String gender = mSpUtils.getParam(SharedPreferencesUtils.GENDER ,SharedPreferencesUtils.GENDER_DEFAULT).toString();
        tv_show_gender.setText(gender);
        String stepgoalTv = mSpUtils.getParam(SharedPreferencesUtils.STEP_GOAL, SharedPreferencesUtils.STEPGOAL_DEFAULT).toString();
        tv_show_goal.setText(stepgoalTv);

        /*gender弹框*/
        view_gender = LayoutInflater.from(getActivity()).inflate(R.layout.pop_gender, null);
        tv_show_gender_male = (TextView) view_gender.findViewById(R.id.male);
        tv_show_gender_femal = (TextView) view_gender.findViewById(R.id.femal);

        /*height弹框*/
        view_height = LayoutInflater.from(getActivity()).inflate(R.layout.popwindow_heght, null);
        height_cancle = (TextView) view_height.findViewById(R.id.height_cancle);
        height_ok = (TextView) view_height.findViewById(R.id.height_ok);
        numberPicker = (NumberPicker) view_height.findViewById(R.id.number_pick);
        //numberPicker.setTextSize(R.dimen.number_picker_size);
        numberPicker.setSelectedTextSize(R.dimen.number_picker_size);
        numberPicker.setTextSize(R.dimen.number_picker_size);
        numberPicker.setDividerThickness(1);

        numberPicker.setMinValue(90);
        numberPicker.setMaxValue(240);


        /*weight弹框*/
        pop_weight = LayoutInflater.from(getActivity()).inflate(R.layout.pop_weight, null);
        weight_cancle = (TextView) pop_weight.findViewById(R.id.cancle);
        weight_ok = (TextView) pop_weight.findViewById(R.id.ok);
        weight_number_pick_right = (NumberPicker) pop_weight.findViewById(R.id.number_pick_right);
        weight_number_pick_left = (NumberPicker) pop_weight.findViewById(R.id.number_pick_left);

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

        /*metric 弹框*/
        pop_metric = LayoutInflater.from(getActivity()).inflate(R.layout.pop_metric, null);
        tv_show_metric_kgcm = (TextView) pop_metric.findViewById(R.id.kg_cm);
        tv_show_metric_lbsft = (TextView) pop_metric.findViewById(R.id.lbs_ft);
        tv_show_metric.setText(getString(R.string.unit_kg) + " / " + getString(R.string.unit_cm));
        tv_show_metric_kgcm.setText(getString(R.string.unit_kg) + " / " + getString(R.string.unit_cm));
        tv_show_metric_lbsft.setText(getString(R.string.unit_lbs) + " / " + getString(R.string.unit_feet));

        /*stepgoal 弹框*/
        pop_stepgoal = LayoutInflater.from(getActivity()).inflate(R.layout.pop_stepgoal, null);
        stepgoal_number_pick = (NumberPicker) pop_stepgoal.findViewById(R.id.numberpicker_stepgoal);
        stepgoal_ok = (TextView) pop_stepgoal.findViewById(R.id.stepgoal_ok);
        stepgoal_cancle = (TextView) pop_stepgoal.findViewById(R.id.stepgoal_cancle);

        stepgoal_number_pick.setMinValue(0);
        stepgoal_number_pick.setMaxValue(goalList.length - 1);
        stepgoal_number_pick.setDisplayedValues(goalList);
        stepgoal_number_pick.setWrapSelectorWheel(true);
        stepgoal_number_pick.setEnabled(true);
        stepgoal_number_pick.setSelectedTextSize(R.dimen.number_picker_size);
        stepgoal_number_pick.setTextSize(R.dimen.number_picker_size);
        stepgoal_number_pick.setDividerThickness(1);

        /*notification bar */
        if (mSpUtils.getParam(SharedPreferencesUtils.SWITCHBAR, SharedPreferencesUtils.SWITCH_TRUE).equals(SharedPreferencesUtils.SWITCH_TRUE)) {
            switchCompat.setChecked(true);
        }
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSpUtils.setParam(SharedPreferencesUtils.SWITCHBAR, SharedPreferencesUtils.SWITCH_TRUE);
                    EventBusUtil.sendEvent(new Event(EventBusUtil.EventCode.STOPNOTIFICATION));
                } else {
                    mSpUtils.setParam(SharedPreferencesUtils.SWITCHBAR, SharedPreferencesUtils.SWITCH_FALSE);
                    EventBusUtil.sendEvent(new Event(EventBusUtil.EventCode.STOPNOTIFICATION));
                }
            }
        });

        /*view 事件*/
        /*gender 弹框子控件*/
        tv_show_gender_male.setOnClickListener(this);
        tv_show_gender_femal.setOnClickListener(this);

        /*height 弹框子控件*/
        height_cancle.setOnClickListener(this);
        height_ok.setOnClickListener(this);
        tv_show_height.setText(mSpUtils.getParam(SharedPreferencesUtils.HEIGHT, SharedPreferencesUtils.Height_DEFAULT) + " "+getString(R.string.unit_cm));
        numberPicker.setValue((Integer) mSpUtils.getParam(SharedPreferencesUtils.HEIGHT, 170));
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                tv_show_height.setText(newVal + " "+getString(R.string.unit_cm));
            }
        });

        /*weight 弹框子控件*/
        weight_cancle.setOnClickListener(this);
        weight_ok.setOnClickListener(this);
        String weight = mSpUtils.getParam(SharedPreferencesUtils.WEIGHT, SharedPreferencesUtils.WEIGHT_DEFAULT).toString();
        tv_show_weight.setText(mSpUtils.getParam(SharedPreferencesUtils.WEIGHT, SharedPreferencesUtils.WEIGHT_DEFAULT) +" "+ getString(R.string.unit_kg));
        weight_number_pick_left.setValue(Integer.valueOf(weight.substring(0, weight.indexOf("."))));
        weight_number_pick_right.setValue(Integer.valueOf(weight.substring(weight.indexOf(".") + 1)));
        //weight_number_pick_left.setValue((Integer) mSpUtils.getParam(SharedPreferencesUtils.WEIGHT ,60));
        weight_number_pick_right.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                /*kgbuilder.delete(0,kgbuilder.length());
                kgbuilder.append(newVal);
                kgbuilder.append*/
                //mWeight_Decimal="."+newVal+" kg";
            }
        });

        weight_number_pick_left.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
               /* mDecimal=newVal+"";
                kgbuilder.delete(kgbuilder.indexOf(".")+1,kgbuilder.length());
                kgbuilder.append(newVal);*/
                //mWeight_Integer=newVal+"";
            }
        });

        /*metric 弹框子控件*/
        tv_show_metric_lbsft.setOnClickListener(this);
        tv_show_metric_kgcm.setOnClickListener(this);

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        tv_show_metric_kgcm.measure(w, h);
        int height = tv_show_metric_kgcm.getMeasuredHeight();
        int width = tv_show_metric_kgcm.getMeasuredWidth();
        tv_show_metric_lbsft.setWidth(tv_show_metric_kgcm.getMeasuredWidth());
        Log.d(TAG, tv_show_metric_kgcm.getMeasuredWidth() + ">>>>>");

        /*stepgoal 弹框子控件*/


        stepgoal_number_pick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.d(TAG, "setonvaluchangedlistenner " + newVal);
            }
        });
        int lastgoalOrder = (Integer) mSpUtils.getParam(SharedPreferencesUtils.STEP_GOAL_ORDER, SharedPreferencesUtils.STEPGOALORDER_DEFAULT);
        stepgoal_number_pick.setValue(lastgoalOrder);
//        stepgoal_number_pick.setec
        stepgoal_cancle.setOnClickListener(this);
        stepgoal_ok.setOnClickListener(this);
    }

    @Override
    protected void setDefaultFragmentTitle(String title) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_gender:
                showPopWindow(view_gender, false, tv_show_gender);
                break;
            case R.id.rl_height:
                showPopWindow(view_height, true, null);
                break;

            case R.id.rl_weight:
                showPopWindow(pop_weight, true, null);
                break;
            case R.id.rl_metric:
                showPopWindow(pop_metric, false, tv_show_metric);
                break;

            case R.id.rl_setp_goal:
                showPopWindow(pop_stepgoal, true, null);
                break;
            /*gender 弹框子控件*/
            case R.id.male:
                tv_show_gender.setText(R.string.male);
                popWindow.dissmiss();
                tv_show_gender_male.setBackgroundColor(getResources().getColor(R.color.set_text_color));
                tv_show_gender_femal.setBackgroundColor(Color.WHITE);
                mSpUtils.setParam(SharedPreferencesUtils.GENDER, SharedPreferencesUtils.MALE);
                Firebase.getInstance(getApplicationContext()).logEvent("设置页", "性别选择", "male");
                break;
            case R.id.femal:
                tv_show_gender.setText(R.string.female);
                popWindow.dissmiss();
                tv_show_gender_femal.setBackgroundColor(getResources().getColor(R.color.set_text_color));
                tv_show_gender_male.setBackgroundColor(Color.WHITE);
                mSpUtils.setParam(SharedPreferencesUtils.GENDER, SharedPreferencesUtils.FEMALE);
                Firebase.getInstance(getApplicationContext()).logEvent("设置页", "性别选择", "female");

                break;

            /*height 弹框子控件*/
            case R.id.height_cancle:
                popWindow.dissmiss();
                break;
            case R.id.height_ok:
                Log.d(TAG, numberPicker.getOrder() + " height _ok");
                popWindow.dissmiss();
                int value = numberPicker.getValue();
                mSpUtils.setParam(SharedPreferencesUtils.HEIGHT, value);
                Firebase.getInstance(getApplicationContext()).logEvent("设置页", "身高设置", String.valueOf(value));
                break;

            /*weight 弹框子控件*/
            case R.id.ok:
                String weightString = weight_number_pick_left.getValue() + "." + weight_number_pick_right.getValue();
                tv_show_weight.setText(weightString + " " + getString(R.string.unit_kg));
                mSpUtils.setParam(SharedPreferencesUtils.WEIGHT, Float.valueOf(weightString));
                Firebase.getInstance(getApplicationContext()).logEvent("设置页", "体重设置", weightString);
                popWindow.dissmiss();
                break;
            case R.id.cancle:
                popWindow.dissmiss();
                break;

            /*metric 弹框子控件*/
            case R.id.kg_cm:
                tv_show_metric.setText(getString(R.string.unit_kg) + " / " + getString(R.string.unit_cm));
                popWindow.dissmiss();
                tv_show_metric_kgcm.setBackgroundColor(getResources().getColor(R.color.set_text_color));
                tv_show_metric_lbsft.setBackgroundColor(Color.WHITE);
                mSpUtils.setParam(SharedPreferencesUtils.M_UNIT, SharedPreferencesUtils.KG_CM);
                break;
            case R.id.lbs_ft:
                tv_show_metric.setText(getString(R.string.unit_lbs) +" / " + getString(R.string.unit_feet));
                popWindow.dissmiss();
                tv_show_metric_lbsft.setBackgroundColor(getResources().getColor(R.color.set_text_color));
                tv_show_metric_kgcm.setBackgroundColor(Color.WHITE);
                mSpUtils.setParam(SharedPreferencesUtils.M_UNIT, SharedPreferencesUtils.LBS_FT);
                break;

                /*stepgoal 弹框子控件*/
            case R.id.stepgoal_cancle:
                popWindow.dissmiss();
                break;
            case R.id.stepgoal_ok:
                String goalText = goalList[stepgoal_number_pick.getValue()];
                Log.d(TAG, "onClick: stepgoal_ok " + goalText + "  order :" + stepgoal_number_pick.getWheelItemCount() + "getvalue :" + stepgoal_number_pick.getValue());
                mSpUtils.setParam(SharedPreferencesUtils.STEP_GOAL, goalText);
                mSpUtils.setParam(SharedPreferencesUtils.STEP_GOAL_ORDER, stepgoal_number_pick.getValue());
                popWindow.dissmiss();
                Firebase.getInstance(getApplicationContext()).logEvent("设置页", "目标设置", goalText);
                setPedometerFragmentStepGoalTV();
                break;
            case R.id.swc:
                switchCompat.setChecked(!switchCompat.isSelected());
                if (switchCompat.isSelected()) {
                    mSpUtils.setParam(SharedPreferencesUtils.SWITCHBAR, SharedPreferencesUtils.SWITCH_TRUE);
                } else {
                    mSpUtils.setParam(SharedPreferencesUtils.SWITCHBAR, SharedPreferencesUtils.SWITCH_FALSE);
                }
                break;

        }
    }

    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.male:
                popWindow.dissmiss();
                break;
            case R.id.femal:
                popWindow.dissmiss();
                break;
        }
    }

    /**
     * @param view_pop
     * @param enableBackgroundDark 是否是大框
     * @param dropDowm
     */
    private void showPopWindow(View view_pop, boolean enableBackgroundDark, View dropDowm) {
        if (!enableBackgroundDark) {
            popWindow = new CustomPopWindow.PopupWindowBuilder(getActivity())
                    .setView(view_pop)//显示的布局，还可以通过设置一个View
//                .size(100,400) //设置显示的大小，不设置就默认包裹内容
                    .setFocusable(true)//是否获取焦点，默认为ture
                    .setOutsideTouchable(true)//是否PopupWindow 以外触摸dissmiss
                    .create();
            popWindow.showAsDropDown(dropDowm, -40, 10);//显示PopupWindow
        } else {
            popWindow = new CustomPopWindow.PopupWindowBuilder(getActivity())
                    .setView(view_pop)//显示的布局，还可以通过设置一个View
                    .enableBackgroundDark(true)
                    //.size(680, 600)
                    .size(320, 300) // dp
                    .setBgDarkAlpha(0.5f)
                    .setFocusable(true)//是否获取焦点，默认为ture
                    .setOutsideTouchable(true)//是否PopupWindow 以外触摸dissmiss
                    .create();
            popWindow.showAtLocation(view, Gravity.CENTER, 0, 10);//显示PopupWindow
        }

    }

    @Override
    public boolean allowShowAd() {
        return false;
    }
}
