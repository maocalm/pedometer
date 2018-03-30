package com.baisi.spedometer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baisi.spedometer.R;
import com.baisi.spedometer.step.bean.StepDataNormal;
import com.baisi.spedometer.step.utils.SharedPreferencesUtils;
import com.baisi.spedometer.step.utils.StepConversion;

import java.util.Date;
import java.util.List;

/**
 * Created by hanwenmao on 2018/1/3.
 */

public class HistoryAdapter extends RecyclerView.Adapter {

    List<StepDataNormal> stepDataNormalsList;
    Context context;
    private static final int TITLE = 0;
    private static final int CONTENT = 1;
    private SharedPreferencesUtils mSpUtils;
    private Float weight;
    private String gender;
    private int height;

    public HistoryAdapter(List<StepDataNormal> stepData, Context context) {
        this.stepDataNormalsList = stepData;
        this.context = context;
        mSpUtils = new SharedPreferencesUtils(context);
        weight = (Float) mSpUtils.getParam(SharedPreferencesUtils.WEIGHT, SharedPreferencesUtils.WEIGHT_DEFAULT);
        gender = (String) mSpUtils.getParam(SharedPreferencesUtils.GENDER, SharedPreferencesUtils.GENDER_DEFAULT);
        height = (Integer) mSpUtils.getParam(SharedPreferencesUtils.HEIGHT, SharedPreferencesUtils.Height_DEFAULT);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater view = LayoutInflater.from(context);
        if (viewType == TITLE) {
            View viewTitle = view.inflate(R.layout.item1_adapter_history_activity, parent, false);
            TitleHolder titleHolder = new TitleHolder(viewTitle);
            return titleHolder;
        } else {
            View viewContent = view.inflate(R.layout.item2_adapter_history_activity, parent, false);
            ContentHolder contentHolder = new ContentHolder(viewContent);
            return contentHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TITLE:
                TitleHolder titleHolder = (TitleHolder) holder;
                Date date = stepDataNormalsList.get(position).getStepTime();
                int step = stepDataNormalsList.get(position).getStep();
                String weekRange = StepConversion.getTodayWeekRange(date);
                if (position==0){
                    titleHolder.timeRange.setText(R.string.this_week);
                }else {
                    titleHolder.timeRange.setText(weekRange);
                }
                titleHolder.steps.setText(step+"");
                break;
            case CONTENT:
                ContentHolder contentHolder = (ContentHolder) holder;
                int step2 = stepDataNormalsList.get(position).getStep();
                int stepSecond2 = stepDataNormalsList.get(position).getStepSecond();
                Date stepTime = stepDataNormalsList.get(position).getStepTime();
                String timeString = StepConversion.getTodaySimple2Date(stepTime);

                contentHolder.date_tv.setText(timeString);
                contentHolder.step_tv.setText(String.valueOf(step2));
                contentHolder.calorle_tv.setText(StepConversion.getCalorie(weight, step2));
                contentHolder.km_tv.setText(StepConversion.getDistance(gender ,step2,height));
                contentHolder.stepsecond_tv.setText(String.valueOf(stepSecond2));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return stepDataNormalsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // return super.getItemViewType(position);
        StepDataNormal stepDataNormal = stepDataNormalsList.get(position);
        if (stepDataNormal.getIsTopTitle()) {
            return TITLE;
        } else {
            return CONTENT;
        }

    }


    class TitleHolder extends RecyclerView.ViewHolder {
        TextView timeRange;
        TextView steps;

        public TitleHolder(View itemView) {
            super(itemView);
            timeRange = itemView.findViewById(R.id.time_range);
            steps = itemView.findViewById(R.id.steps_tv);
        }
    }


    class ContentHolder extends RecyclerView.ViewHolder {
        TextView date_tv;
        TextView step_tv;
        TextView calorle_tv;
        TextView calorle_unit_tv;
        TextView km_tv;
        TextView km_unit_tv;
        TextView stepsecond_tv;
        TextView stepsecond_unit_tv;

        public ContentHolder(View itemView) {
            super(itemView);
            date_tv = itemView.findViewById(R.id.date_tv);
            step_tv = itemView.findViewById(R.id.step_tv);
            calorle_tv = itemView.findViewById(R.id.calorle_tv);
            calorle_unit_tv = itemView.findViewById(R.id.calorle_unit_tv);
            km_tv = itemView.findViewById(R.id.km_tv);
            km_unit_tv = itemView.findViewById(R.id.km_unit_tv);
            stepsecond_tv = itemView.findViewById(R.id.stepsecond_tv);
            stepsecond_unit_tv = itemView.findViewById(R.id.stepsecond_unit_tv);
        }
    }

}
