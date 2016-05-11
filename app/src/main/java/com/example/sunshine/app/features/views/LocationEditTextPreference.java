package com.example.sunshine.app.features.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sunshine.app.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

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

        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int result = availability.isGooglePlayServicesAvailable(getContext());

        if (result == ConnectionResult.SUCCESS) {
            setWidgetLayoutResource(R.layout.pref_current_location);
            Log.d("LocationPreference", "Google api available");
        }

        Log.d("LocationPreference", "minLength = " + mMinLength);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        View currentLocation = view.findViewById(R.id.current_location);
        currentLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "View created!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Dialog dialog = getDialog();
                if (dialog instanceof AlertDialog) {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    if (editable.length() < mMinLength) {
                        button.setEnabled(false);
                    } else {
                        button.setEnabled(true);
                    }
                }
            }
        });
    }
}
