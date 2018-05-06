package com.example.laurens.meet_combi;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.UUID;

public class FunctionGen extends ContentFragment
        implements View.OnTouchListener {

    private final String DEBUG_TAG = "FunctionGen";

    // Bluetooth connection
    private Bluetooth.ConnectCallbacks mBluetoothCallbacks;
    private final UUID mUuidServiceFunctionGen = UUID.fromString("8a37da8a-bd28-41fe-8aa3-74afa5b60b80");

    // Callbacks
    private Callbacks mCallbacks;
    public interface Callbacks {
        Bluetooth getBluetooth();
    }

    // Frequency display
    private TextView mFrequencyDisplay;
    private DecimalFormat df;
    private int frequency = 0;

    // Button handler
    private final byte INCREMENT_FREQUENCY = 1;
    private final byte DECREMENT_FREQUENCY = 2;
    private final byte SET_FREQUENCY = 3;

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

                    final EditText mEditText = new EditText(getActivity());
                    LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    mEditText.setLayoutParams(mLayoutParams);
                    mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    mEditText.setText("" + frequency, TextView.BufferType.EDITABLE);
                    dialogBuilder.setView(mEditText);

                    dialogBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int buttonPressed) {
                            try {
                                frequency = Integer.parseInt(mEditText.getText().toString());
                                changeFrequency(SET_FREQUENCY);
                            } catch (NumberFormatException nfe) {
                                Log.e("Frequency Input", "Could not parse input.");
                            }
                            dialogInterface.dismiss();
                        }
                    });

                    dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int buttonPressed) {
                            dialogInterface.dismiss();
                        }
                    });

                    dialog = dialogBuilder.create();
                    dialog.show();
                }
            }
        });

        // Get bluetooth connection
        mBluetoothCallbacks = new Bluetooth.ConnectCallbacks() {

            @Override
            public void onScanDone(Boolean deviceFound) {

            }

            @Override
            public void onConnectingDone(Boolean deviceConnected) {

            }

            @Override
            public void onServicesDiscovered() {
                Bluetooth mBluetooth = mCallbacks.getBluetooth();

                BluetoothGattService mBluetoothGattService = mBluetooth.getService(mUuidServiceFunctionGen);

                //Log.d(DEBUG_TAG+".onServicesDiscovered", "Reading all characteristics of service " + mUuidServiceFunctinalityAvailable.toString());
                //for (BluetoothGattCharacteristic mBluetoothGattCharacteristic : mBluetoothGattService.getCharacteristics()) {
                //    Log.d(DEBUG_TAG+".onServicesDiscovered", mBluetoothGattCharacteristic.getUuid().toString());
                //}

                if (mBluetoothGattService == null) {
                    Log.d(DEBUG_TAG+".onServicesDiscovered", "Service not found");
                } else {
                    // Start discovering characteristics
                    Log.d(DEBUG_TAG+".onServicesDiscovered", "Start discovering characteristics");
                    // TODO: check for characterstics
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGattCharacteristic characteristicReaded) {

            }
        };
        mCallbacks.getBluetooth().addCallback(mBluetoothCallbacks);
        mCallbacks.getBluetooth().discoverServices();

        return v;
    }

    @Override
    public void onDestroyView() {

        mCallbacks.getBluetooth().removeCallback(mBluetoothCallbacks);
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        mCallbacks = (Callbacks) getActivity();
    }

    @Override
    public void onDetach() {

        super.onDetach();
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