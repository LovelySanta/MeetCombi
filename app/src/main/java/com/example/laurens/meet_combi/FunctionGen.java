package com.example.laurens.meet_combi;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.UUID;

public class FunctionGen extends ContentFragment
        implements View.OnTouchListener {

    private TextView mFrequencyDisplay;
    private DecimalFormat df;
    private int frequency = 1234567;

    // Button handler
    private final byte INCREMENT_FREQUENCY = 1;
    private final byte DECREMENT_FREQUENCY = 2;
    private final byte SET_FREQUENCY = 3;

    // Bluetooth interface
    //private Bluetooth.Callbacks mBluetoothCallbacks;
    private final UUID SERVICE_TEST = UUID.fromString("70d8530a-4743-402e-b5ce-a107946a550f");
    private final UUID CHARACTERISTIC_POS = UUID.fromString("004dceee-764c-4ab6-9a52-fd36ffdb70d5");
    private final UUID CHARACTERISTIC_SET_POS = UUID.fromString("23fd46df-8dec-4899-8af4-7f113f62df10");

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parentVG, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_functiongen, parentVG, false);

        // Init decimalFormat
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator(' ');
        df = new DecimalFormat();
        df.setDecimalFormatSymbols(dfs);
        df.setGroupingSize(3);
        df.setMaximumFractionDigits(0);

        // Button increment frequency
        Button mFrequencyButtonPlus = (Button) v.findViewById(R.id.FunctionGen_FrequencyButtonPlus);
        mFrequencyButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFrequency(INCREMENT_FREQUENCY);
            }
        });

        // Button decrement frequency
        Button mFrequencyButtonMinus = (Button) v.findViewById(R.id.FunctionGen_FrequencyButtonMinus);
        mFrequencyButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFrequency(DECREMENT_FREQUENCY);
            }
        });

        // Display
        mFrequencyDisplay = (TextView) v.findViewById(R.id.FunctionGen_FrequencyDisplay);
        mFrequencyDisplay.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.FuctieGen_FrequencyScreen));
        changeFrequency(SET_FREQUENCY);
        mFrequencyDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = null;

                if (!getActivity().isFinishing()) {
                    // https://developer.android.com/reference/android/app/Dialog
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                    dialogBuilder
                            .setTitle("Functiegenerator")
                            .setMessage("Input new frequency.");

                    dialogBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int buttonPressed) {

                            switch (buttonPressed) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                default:
                                    break;
                            }

                            dialogInterface.dismiss();
                        }
                    });

                    dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int buttonPressed) {

                            switch (buttonPressed) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                default:
                                    break;
                            }

                            dialogInterface.dismiss();
                        }
                    });

                    dialog = dialogBuilder.create();
                    dialog.show();
                }
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //mBluetoothCallbacks = (Bluetooth.Callbacks) getActivity();
    }

    private void changeFrequency(byte changeToMake) {
        switch (changeToMake) {
            case INCREMENT_FREQUENCY:
                mFrequencyDisplay.setText(df.format(++frequency)+" Hz ");
                break;

            case DECREMENT_FREQUENCY:
                mFrequencyDisplay.setText(df.format(--frequency)+" Hz ");
                break;

            case SET_FREQUENCY:
                mFrequencyDisplay.setText(df.format(frequency)+" Hz ");
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
}