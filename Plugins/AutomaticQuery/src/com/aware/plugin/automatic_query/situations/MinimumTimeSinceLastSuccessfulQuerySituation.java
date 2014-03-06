package com.aware.plugin.automatic_query.situations;

import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aware.Aware;

import java.util.Map;

import de.unipassau.mics.contextopheles.base.ContextophelesConstants;
import de.unipassau.mics.contextopheles.utils.CommonSettings;

/**
 * Created by wmb on 10.01.14.
 */
public class MinimumTimeSinceLastSuccessfulQuerySituation extends Situation {
    int DEFAULT_TIME = 10000;
    public final String TIME_KEY = this.PREFIX + "_TIME";
    private EditText timeValue;
    private SeekBar timeSeekBar;

    public MinimumTimeSinceLastSuccessfulQuerySituation(){
        this.TAG = "MinimumTimeSinceLastSuccessfulQuerySituation";
        this.PREFIX = "SITUATION_MINIMUM_TIME";
    }

    public boolean assess(Map<String, Object> contextMap, Context context) {
        if (validateContext(contextMap)) {
            Long lastSuccessfulQuery = (Long) contextMap.get(ContextophelesConstants.INFO_AQ_LAST_SUCCESSFUL_QUERY);

            // returns true, when last successful query was more then 15 seconds ago
            return lastSuccessfulQuery + getMinimumTimeSetting(context) > System.currentTimeMillis();
        } else {
            return false;
        }
    }

    private boolean validateContext(Map<String, Object> contextMap) {
        boolean result = true;
        result &= validateSingleContextInformation(contextMap, ContextophelesConstants.INFO_AQ_LAST_SUCCESSFUL_QUERY, Long.class.toString());
        return result;
    }

    public LinearLayout getSettingsLayout(Context context){
        final Context finalContext = context;

        LinearLayout result = super.getSettingsLayout(context);

        timeSeekBar = new SeekBar(context);
        timeValue = new EditText(context);

        timeValue.setId(348543);
        timeValue.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Medium_Inverse);

        // TimeValueLabel
        TextView timeValueLabel= new TextView(context);
        timeValueLabel.setText("Minimum Time (Sec)");
        timeValueLabel.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Medium_Inverse);


        // Relative Layout for TimeValue
        RelativeLayout timeValueLayout = new RelativeLayout(context);
        timeValueLayout.addView(timeValue);
        timeValueLayout.addView(timeValueLabel);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) timeValueLabel.getLayoutParams();

        params.addRule(RelativeLayout.RIGHT_OF, timeValue.getId());
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        timeValueLabel.setLayoutParams(params);



        result.addView(timeSeekBar);
        result.addView(timeValueLayout);

        timeSeekBar.setMax(600);

        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                Aware.setSetting(finalContext.getContentResolver(), TIME_KEY, i * 1000);

                timeValue.setText("" + (getMinimumTimeSetting(finalContext) / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });

        return result;
    }

    public void initSettings(Context context){
        timeSeekBar.setProgress((int) (getMinimumTimeSetting(context)/1000));
        timeValue.setText("" + (getMinimumTimeSetting(context) / 1000));
        super.initSettings(context);
    }

    public void resetSettings(Context context) {
        Aware.setSetting(context.getContentResolver(), TIME_KEY, DEFAULT_TIME);
        super.resetSettings(context);
    }

    private long getMinimumTimeSetting(Context context){
       return CommonSettings.getLongFromAwareSettings(context.getContentResolver(), TIME_KEY, DEFAULT_TIME);
    }
}
