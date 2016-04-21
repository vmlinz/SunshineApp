package com.example.sunshine.app.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.util.Log;

import com.example.sunshine.app.R;

/**
 * Created by vmlinz on 4/21/16.
 */
public class LocationEditTextPreference extends EditTextPreference {
    private final int mMinLength;

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0, 0
        );

        try {
            mMinLength = ta.getInteger(R.styleable.LocationEditTextPreference_minLength, 0);
        } finally {
            ta.recycle();
        }

        Log.d("LocationPreference", "minLength = " + mMinLength);
    }
}
