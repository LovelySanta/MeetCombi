package com.example.laurens.meet_combi;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FunctionGen extends ContentFragment {

    private final String DEBUG_TAG = "FunctionGen";

    // Bluetooth connection
    private Bluetooth.ConnectCallbacks mBluetoothCallbacks;
    private final UUID mUuidServiceFunctionGen = UUID.fromString("8a37da8a-bd28-41fe-8aa3-74afa5b60b80");
    private final UUID mUuidCharacteristicFunctionGenFrequency = UUID.fromString("8a37da8a-bd28-41fe-8aa3-74afa5b60b82");
    private final UUID mUuidCharacteristicFunctonGenSignalShape   = UUID.fromString("8a37da8a-bd28-41fe-8aa3-74afa5b60b85");
    private final UUID mUuidCharacteristicFunctionGenAmplitude = UUID.fromString("8a37da8a-bd28-41fe-8aa3-74afa5b60b81");
    private final UUID mUuidCharacteristicFunctionGenOffset = UUID.fromString("8a37da8a-bd28-41fe-8aa3-74afa5b60b83");
    private final UUID mUuidCharacteristicFunctonGenOutputEnabled = UUID.fromString("8a37da8a-bd28-41fe-8aa3-74afa5b60b84");


    // Callbacks
    private Callbacks mCallbacks;
    public interface Callbacks {
        Bluetooth getBluetooth();
    }
    private DecimalFormat df;

    // Frequency display
    private TextView mFrequencyDisplay;
    private int mFrequency = 0;
    private final byte CHANGE_FREQUENCY = 1;

    // Amplitude display
    private TextView mAmplitudeDisplay;
    private int mAmplitude = 0;
    private final byte CHANGE_AMPLITUDE = 2;

    // Offset display
    private TextView mOffsetDisplay;
    private int mOffset = 0;
    private final byte CHANGE_OFFSET = 3;

    // Enable output button
    private Button mEnableOutputButton;
    private Boolean mEnableOutput = false;
    private final byte CHANGE_OUTPUT = 4;

    // Waveform shape buttons
    private Button mShapeSinusButton;
    private Button mShapeTriangleButton;
    private Button mShapeSquareButton;
    private Button mShapeSawtoothButton;
    private final int mShapeSinus = 1;
    private final int mShapeTriangle = 2;
    private final int mShapeSquare = 3;
    private final int mShapeSawtooth = 4;
    private int mShape = mShapeSinus;
    private final byte CHANGE_SHAPE = 5;


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

        initFrequency(v); // Frequency
        initAmplitude(v); // Amplitude
        initOffset(v);    // Offset
        initShape(v);     // Shape

        initEnableOutput(v); // Enable output button

        // Get bluetooth connection
        initBluetooth();

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

    private void initFrequency(View v) {

        // Button increment frequency
        Button mFrequencyButtonPlus = v.findViewById(R.id.functionGen_FrequencyButtonPlus);
        mFrequencyButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFrequency++;
                changeSetting(CHANGE_FREQUENCY, true);
            }
        });

        // Button decrement frequency
        Button mFrequencyButtonMinus = v.findViewById(R.id.functionGen_FrequencyButtonMinus);
        mFrequencyButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFrequency--;
                changeSetting(CHANGE_FREQUENCY, true);
            }
        });

        // Display
        mFrequencyDisplay = v.findViewById(R.id.functionGen_FrequencyDisplay);
        mFrequencyDisplay.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.fuctionGen_Screen));
        changeSetting(CHANGE_FREQUENCY, false);
        mFrequencyDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                    mEditText.setText(String.valueOf(mFrequency), TextView.BufferType.EDITABLE);
                    dialogBuilder.setView(mEditText);

                    dialogBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int buttonPressed) {
                            try {
                                mFrequency = Integer.parseInt(mEditText.getText().toString());
                                changeSetting(CHANGE_FREQUENCY, true);
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

                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                }
            }
        });
    }
    private void initAmplitude(View v) {

        // Button increment amplitude
        Button mFrequencyButtonPlus = v.findViewById(R.id.functionGen_AmplitudeButtonPlus);
        mFrequencyButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAmplitude++;
                changeSetting(CHANGE_AMPLITUDE, true);
            }
        });

        // Button decrement amplitude
        Button mFrequencyButtonMinus = v.findViewById(R.id.functionGen_AmplitudeButtonMinus);
        mFrequencyButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAmplitude--;
                changeSetting(CHANGE_AMPLITUDE, true);
            }
        });

        // Display
        mAmplitudeDisplay = v.findViewById(R.id.functionGen_AmplitudeDisplay);
        mAmplitudeDisplay.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.fuctionGen_Screen));
        changeSetting(CHANGE_AMPLITUDE, false);
        mAmplitudeDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!getActivity().isFinishing()) {
                    // https://developer.android.com/reference/android/app/Dialog
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                    dialogBuilder
                            .setTitle("Functiegenerator")
                            .setMessage("Input new amplitude.");

                    final EditText mEditText = new EditText(getActivity());
                    LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    mEditText.setLayoutParams(mLayoutParams);
                    mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    mEditText.setText(String.valueOf(mAmplitude), TextView.BufferType.EDITABLE);
                    dialogBuilder.setView(mEditText);

                    dialogBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int buttonPressed) {
                            try {
                                mAmplitude = Integer.parseInt(mEditText.getText().toString());
                                changeSetting(CHANGE_AMPLITUDE, true);
                            } catch (NumberFormatException nfe) {
                                Log.e("Amplitude Input", "Could not parse input.");
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

                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                }
            }
        });
    }
    private void initOffset(View v) {

        // Button increment amplitude
        Button mFrequencyButtonPlus = v.findViewById(R.id.functionGen_OffsetButtonPlus);
        mFrequencyButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOffset++;
                changeSetting(CHANGE_OFFSET, true);
            }
        });

        // Button decrement amplitude
        Button mFrequencyButtonMinus = v.findViewById(R.id.functionGen_OffsetButtonMinus);
        mFrequencyButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOffset--;
                changeSetting(CHANGE_OFFSET, true);
            }
        });

        // Display
        mOffsetDisplay = v.findViewById(R.id.functionGen_OffsetDisplay);
        mOffsetDisplay.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.fuctionGen_Screen));
        changeSetting(CHANGE_OFFSET, false);
        mOffsetDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!getActivity().isFinishing()) {
                    // https://developer.android.com/reference/android/app/Dialog
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                    dialogBuilder
                            .setTitle("Functiegenerator")
                            .setMessage("Input new offset.");

                    final EditText mEditText = new EditText(getActivity());
                    LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    mEditText.setLayoutParams(mLayoutParams);
                    mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    mEditText.setText(String.valueOf(mOffset), TextView.BufferType.EDITABLE);
                    dialogBuilder.setView(mEditText);

                    dialogBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int buttonPressed) {
                            try {
                                mOffset = Integer.parseInt(mEditText.getText().toString());
                                changeSetting(CHANGE_OFFSET, true);
                            } catch (NumberFormatException nfe) {
                                Log.e("Offset Input", "Could not parse input.");
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

                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                }
            }
        });
    }
    private void initShape(View v) {
        // Button shape sinus
        mShapeSinusButton = v.findViewById(R.id.functionGen_ShapeSinus);
        mShapeSinusButton.setText(R.string.functionGen_shape_sinus);
        mShapeSinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShape = mShapeSinus;
                changeSetting(CHANGE_SHAPE, true);
            }
        });

        // Button shape triangle
        mShapeTriangleButton = v.findViewById(R.id.functionGen_ShapeTriangle);
        mShapeTriangleButton.setText(R.string.functionGen_shape_triangle);
        mShapeTriangleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShape = mShapeTriangle;
                changeSetting(CHANGE_SHAPE, true);
            }
        });

        // Button shape square
        mShapeSquareButton = v.findViewById(R.id.functionGen_ShapeSquare);
        mShapeSquareButton.setText(R.string.functionGen_shape_square);
        mShapeSquareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShape = mShapeSquare;
                changeSetting(CHANGE_SHAPE, true);
            }
        });

        // Button shape sawtooth
        mShapeSawtoothButton = v.findViewById(R.id.functionGen_ShapeSawtooth);
        mShapeSawtoothButton.setText(R.string.functionGen_shape_sawtooth);
        mShapeSawtoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShape = mShapeSawtooth;
                changeSetting(CHANGE_SHAPE, true);
            }
        });

        // Update buttons to default setting
        changeSetting(CHANGE_SHAPE, false);
    }
    private void initEnableOutput(View v) {
        mEnableOutputButton = v.findViewById(R.id.functionGen_EnableOutput);
        changeSetting(CHANGE_OUTPUT, false);
        mEnableOutputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEnableOutput = !mEnableOutput;
                changeSetting(CHANGE_OUTPUT, true);
            }
        });
    }
    private void initBluetooth() {
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

                //Log.d(DEBUG_TAG+".onServicesDiscovered", "Reading all characteristics of service " + mUuidServiceFunctionalityAvailable.toString());
                //for (BluetoothGattCharacteristic mBluetoothGattCharacteristic : mBluetoothGattService.getCharacteristics()) {
                //    Log.d(DEBUG_TAG+".onServicesDiscovered", mBluetoothGattCharacteristic.getUuid().toString());
                //}

                if (mBluetoothGattService == null) {
                    Log.d(DEBUG_TAG+".onServicesDiscovered", "Service not found");
                } else {
                    // Start discovering characteristics
                    Log.d(DEBUG_TAG+".onServicesDiscovered", "Start discovering characteristics");

                    List<UUID> mCharacteristicsToRead = new ArrayList<>();
                    mCharacteristicsToRead.add(mUuidCharacteristicFunctionGenFrequency);
                    mCharacteristicsToRead.add(mUuidCharacteristicFunctonGenSignalShape);
                    mCharacteristicsToRead.add(mUuidCharacteristicFunctionGenAmplitude);
                    mCharacteristicsToRead.add(mUuidCharacteristicFunctionGenOffset);
                    mCharacteristicsToRead.add(mUuidCharacteristicFunctonGenOutputEnabled);
                    mCallbacks.getBluetooth().readCharacteristic(mBluetoothGattService, mCharacteristicsToRead);
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGattCharacteristic characteristic) {
                UUID characteristicUuid = characteristic.getUuid();
                Log.d(DEBUG_TAG+".onCharacteristicRead", "Characteristic " + characteristicUuid.toString() + " has been read successfully.");

                // Frequency value
                if (characteristicUuid.equals(mUuidCharacteristicFunctionGenFrequency)) {
                    try{
                        mFrequency = Integer.parseInt(characteristic.getStringValue(0));
                        changeSetting(CHANGE_FREQUENCY, false);
                    } catch (NumberFormatException nfe) {
                        Log.e(DEBUG_TAG+".onCharacteristicRead", "Could not read the frequency correctly.");
                    }

                // Amplitude value
                } else if (characteristicUuid.equals(mUuidCharacteristicFunctionGenAmplitude)) {
                    try{
                        mAmplitude = Integer.parseInt(characteristic.getStringValue(0));
                        changeSetting(CHANGE_AMPLITUDE, false);
                    } catch (NumberFormatException nfe) {
                        Log.e(DEBUG_TAG+".onCharacteristicRead", "Could not read the amplitude correctly.");
                    }

                // Offset value
                } else if (characteristicUuid.equals(mUuidCharacteristicFunctionGenOffset)) {
                    try{
                        mOffset = Integer.parseInt(characteristic.getStringValue(0));
                        changeSetting(CHANGE_OFFSET, false);
                    } catch (NumberFormatException nfe) {
                        Log.e(DEBUG_TAG+".onCharacteristicRead", "Could not read the offset correctly.");
                    }

                // Enable output
                } else if (characteristicUuid.equals(mUuidCharacteristicFunctonGenOutputEnabled)) {
                    mEnableOutput = characteristic.getStringValue(0).toLowerCase().equals("true");
                    changeSetting(CHANGE_OUTPUT, false);

                // Shape of the waveform
                } else if (characteristicUuid.equals(mUuidCharacteristicFunctonGenSignalShape)) {
                    try{
                        mShape = Integer.parseInt(characteristic.getStringValue(0));
                        changeSetting(CHANGE_SHAPE, false);
                    } catch (NumberFormatException nfe) {
                        Log.e(DEBUG_TAG+".onCharacteristicRead", "Could not read the shape correctly.");
                    }
                }

                // TODO: other characteristics
            }

            @Override
            public void onCharacteristicWrite(BluetoothGattCharacteristic characteristicWritten) {

            }
        };
        mCallbacks.getBluetooth().addCallback(mBluetoothCallbacks);
        mCallbacks.getBluetooth().discoverServices();
    }

    private void changeSetting(final byte changeToMake, Boolean updateBluetooth) {
        // Update TextView
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            // On UI thread.
            switch (changeToMake) {

                case CHANGE_FREQUENCY:
                    mFrequencyDisplay.setText(df.format(mFrequency) + " Hz ");
                    break;

                case CHANGE_AMPLITUDE:
                    mAmplitudeDisplay.setText(df.format(mAmplitude) + " mV");
                    break;

                case CHANGE_OFFSET:
                    mOffsetDisplay.setText(df.format(mOffset) + " mV");
                    break;

                case CHANGE_OUTPUT:
                    mEnableOutputButton.setText(mEnableOutput ? "Disable" : "Enable");
                    break;

                case CHANGE_SHAPE:
                    mShapeSinusButton.setEnabled(mShape == mShapeSinus);
                    mShapeTriangleButton.setEnabled(mShape == mShapeTriangle);
                    mShapeSquareButton.setEnabled(mShape == mShapeSquare);
                    mShapeSawtoothButton.setEnabled(mShape == mShapeSawtooth);
                    break;

                default:
                    break;
            }
        } else {
            // Not on UI thread.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeSetting(changeToMake, false);
                }
            });
        }

        // Update Bluetooth
        if (updateBluetooth) {
            Log.d(DEBUG_TAG+".changeSetting", "Setting " + CHANGE_FREQUENCY + " has been requested to write bluetooth.");

            switch (changeToMake) {

                case CHANGE_FREQUENCY:
                    mCallbacks.getBluetooth().writeCharacteristic(mCallbacks.getBluetooth().getService(mUuidServiceFunctionGen), mUuidCharacteristicFunctionGenFrequency, (Integer) mFrequency);
                    break;

                case CHANGE_AMPLITUDE:
                    mCallbacks.getBluetooth().writeCharacteristic(mCallbacks.getBluetooth().getService(mUuidServiceFunctionGen), mUuidCharacteristicFunctionGenAmplitude, (Integer) mAmplitude);
                    break;

                case CHANGE_OFFSET:
                    mCallbacks.getBluetooth().writeCharacteristic(mCallbacks.getBluetooth().getService(mUuidServiceFunctionGen), mUuidCharacteristicFunctionGenOffset, (Integer) mOffset);
                    break;

                case CHANGE_OUTPUT:
                    mCallbacks.getBluetooth().writeCharacteristic(mCallbacks.getBluetooth().getService(mUuidServiceFunctionGen), mUuidCharacteristicFunctonGenOutputEnabled, (Boolean) mEnableOutput);
                    break;

                default:
                    break;
            }
        }
    }

}