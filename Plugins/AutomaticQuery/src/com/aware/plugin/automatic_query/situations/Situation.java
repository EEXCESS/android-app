package com.aware.plugin.automatic_query.situations;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.aware.Aware;

import java.util.Map;

import de.unipassau.mics.contextopheles.utils.CommonSettings;

/**
 * Created by wmb on 10.01.14.
 */
public abstract class Situation {

    protected String TAG = "Situation";
    protected boolean ENABLED_BY_DEFAULT = true;
    public String PREFIX = "NOT_CHANGED";
    protected ToggleButton enableButton = null;

    public abstract boolean assess(Map<String, Object> contextMap, Context context);

    // Checks, wether the given map contains a not-null object for the given identifier with the given class
    protected boolean validateSingleContextInformation(Map<String, Object> contextMap, String contextIdentifier, String expectedClass) {
        Log.wtf(TAG, "Validating Contextinformation " + contextIdentifier);

        Object obj = contextMap.get(contextIdentifier);

        if (obj != null) {
            Log.wtf(TAG, "Object is not null");
            if (obj.getClass().toString().equals(expectedClass)) {
                Log.wtf(TAG, "Object is of expected class " + expectedClass);
                return true;
            } else {
                Log.wtf(TAG, "Object is not of expected class " + expectedClass + " but of class " + obj.getClass().toString());
                return false;
            }
        } else {
            Log.wtf(TAG, "Object is null");
            return false;
        }
    }

    public LinearLayout getSettingsLayout(Context context){
        final Context finalContext = context;
        LinearLayout result = new LinearLayout(context);
        result.setOrientation(LinearLayout.VERTICAL);

        // add a divider
        ImageView divider = new ImageView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 5);
        lp.setMargins(10, 10, 10, 10);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(Color.GRAY);
        result.addView(divider);

        TextView heading = new TextView(context);
        heading.setText(TAG + " Settings");
        heading.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Large_Inverse );

        result.addView(heading);

        RelativeLayout enableLayout = new RelativeLayout(context);


        this.enableButton = new ToggleButton(context);

        //explicitly set id
        enableButton.setId(1234);

        //enableButton.setEnabled(context);r
        enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Aware.setSetting(finalContext.getContentResolver(),  PREFIX + "_ENABLED", enableButton.isChecked());
            }
        });

        enableLayout.addView(enableButton);

        TextView enableLabel= new TextView(context);
        enableLabel.setText("Enable Situation");

        enableLayout.addView(enableLabel);

        result.addView(enableLayout);


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)enableLabel.getLayoutParams();

        params.addRule(RelativeLayout.RIGHT_OF, enableButton.getId());
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        enableLabel.setLayoutParams(params);


        enableLabel.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Medium_Inverse);

        return  result;
    }

    protected void initSettings(Context context) {
        enableButton.setChecked(isEnabled(context));
    }

    protected void resetSettings(Context context) {
        Aware.setSetting(context.getContentResolver(), this.PREFIX + "_ENABLED", this.ENABLED_BY_DEFAULT);
    }

    protected boolean isEnabled(Context context){
        return CommonSettings.getBooleanFromAwareSettings(context.getContentResolver(), this.PREFIX + "_ENABLED", this.ENABLED_BY_DEFAULT);
    }
}
