package com.example.laurens.meet_combi;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.UUID;

public class Welcome extends ContentFragment {

    private final String DEBUG_TAG = "Welcome";

    // Service describing what functionality is included in this device
    private UUID mUuidServiceFunctionalityAvailable = UUID.fromString("1536a95e-4a5d-47fb-967d-2f053b5744b3");
    // Characteristics in the service
    private UUID mUuidCharacteristicFunctionGen = UUID.fromString("8a37da8a-bd28-41fe-8aa3-74afa5b60b80");
    //private UUID mUuidCharacteristicOsciloscope = UUID.fromString("536a95e-4a5d-47fb-967d-2f053b5744b4"); -- TODO @Francis
    private UUID mUuidCharacteristicMultimeter  = UUID.fromString("49cb47f9-edee-4b02-8091-29d9766fa66a");

    private Bluetooth.ConnectCallbacks mBluetoothCallbacks;
    private Callbacks mCallbacks;
    public interface Callbacks {
        Bluetooth getBluetooth();
        void enableFunction(int functionID, Boolean functionEnabled);
    }

    // Table with status of all modules
    private final int FUNCTION_STATUS_UNCHECKED = 0;
    private final int FUNCTION_STATUS_AVAILABLE = 1;
    private final int FUNCTION_STATUS_UNAVAILABLE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parentVG, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_welcome, parentVG, false);

        // Table with statusses
        final TextView mOsciloscope = v.findViewById(R.id.welcome_table_oscilloscopeStatus);
        final TextView mFunctieGenStatus = v.findViewById(R.id.welcome_table_functieGenStatus);
        final TextView mMultimeterStatus = v.findViewById(R.id.welcome_table_multimeterStatus);

        // Button connect
        Button mScanDevicesButton = v.findViewById(R.id.welcome_btnScanDevices);
        if (mCallbacks.getBluetooth().getConnectionStatus()) {
            // Disable button if connection has been made already
            setConnectButton(mScanDevicesButton, R.string.welcome_connected, false);
            // Check status
            BluetoothGattService mBluetoothGattService = mCallbacks.getBluetooth().getService(mUuidServiceFunctionalityAvailable);
            if (mBluetoothGattService != null) {
                Log.d(DEBUG_TAG+".onCreateView", "Start discovering characteristics since service is discovered.");
                discoverActiveModules(mCallbacks.getBluetooth(), mBluetoothGattService);
            }
        } else {
            setConnectButton(mScanDevicesButton, R.string.welcome_connect, true);
            // Set services unchecked for now...
            setStatus(mOsciloscope, FUNCTION_STATUS_UNCHECKED);
            setStatus(mFunctieGenStatus, FUNCTION_STATUS_UNCHECKED);
            setStatus(mMultimeterStatus, FUNCTION_STATUS_UNCHECKED);
        }
        mScanDevicesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final Button mScanDevicesButton = v.findViewById(R.id.welcome_btnScanDevices);
                mScanDevicesButton.setText(R.string.welcome_connecting);
                mScanDevicesButton.setEnabled(false);

                mBluetoothCallbacks = new Bluetooth.ConnectCallbacks() {

                    @Override
                    public void onScanDone(Boolean deviceFound) {
                        if (deviceFound) {
                            // Create connection
                            mCallbacks.getBluetooth().createBluetoothGatt();
                        } else {
                            // Failed to find device
                            setConnectButton(mScanDevicesButton, R.string.welcome_connect, true);
                        }
                    }

                    @Override
                    public void onConnectingDone(Boolean deviceConnected) {
                        if (deviceConnected) {
                            // We are now connected
                            setConnectButton(mScanDevicesButton, R.string.welcome_connected, false);

                            // Start discovering services
                            mCallbacks.getBluetooth().discoverServices();
                        } else {
                            // Failed to connect...
                            setConnectButton(mScanDevicesButton, R.string.welcome_connect, true);
                        }
                    }

                    @Override
                    public void onServicesDiscovered() {
                        Bluetooth mBluetooth = mCallbacks.getBluetooth();

                        BluetoothGattService mBluetoothGattService = mBluetooth.getService(mUuidServiceFunctionalityAvailable);

                        //Log.d(DEBUG_TAG+".onServicesDiscovered", "Reading all characteristics of service " + mUuidServiceFunctinalityAvailable.toString());
                        //for (BluetoothGattCharacteristic mBluetoothGattCharacteristic : mBluetoothGattService.getCharacteristics()) {
                        //    Log.d(DEBUG_TAG+".onServicesDiscovered", mBluetoothGattCharacteristic.getUuid().toString());
                        //}

                        if (mBluetoothGattService == null) {
                            Log.d(DEBUG_TAG+".onServicesDiscovered", "Service not found");
                        } else {
                            // Start discovering characteristics
                            Log.d(DEBUG_TAG+".onServicesDiscovered", "Start discovering characteristics");
                            discoverActiveModules(mBluetooth, mBluetoothGattService);
                        }
                    }

                    @Override
                    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic) {
                        UUID characteristicUuid = characteristic.getUuid();
                        Log.d(DEBUG_TAG+".onCharacteristicRead", "Characteristic " + characteristicUuid.toString() + " has been read successfully.");

                        if (characteristicUuid.toString().equals(mUuidCharacteristicFunctionGen.toString())) {
                            // Function generator
                            Log.d(DEBUG_TAG+".onCharacteristicRead", "FunctionGenerator has been read.");
                            final Boolean mStatus = characteristic.getStringValue(0).toLowerCase().equals("true");

                            // Set TextView accordingly
                            setStatus(mFunctieGenStatus, mStatus ? FUNCTION_STATUS_AVAILABLE : FUNCTION_STATUS_UNAVAILABLE);

                            // Set navigation button active
                            mCallbacks.enableFunction(R.id.functionGen, mStatus);


                        } else if (characteristicUuid.toString().equals(mUuidCharacteristicMultimeter.toString())) {
                            // Multimeter
                            Log.d(DEBUG_TAG+".onCharacteristicRead", "Multimeter has been read.");
                            final Boolean mStatus = characteristic.getStringValue(0).toLowerCase().equals("true");

                            // Set TextView accordingly
                            setStatus(mMultimeterStatus, mStatus ? FUNCTION_STATUS_AVAILABLE : FUNCTION_STATUS_UNAVAILABLE);

                            // Set navigation button active
                            mCallbacks.enableFunction(R.id.multimeter, mStatus);
                        }
                    }
                };
                mCallbacks.getBluetooth().scanLeDevice(mBluetoothCallbacks);
            }
        });

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

    private void setStatus(final TextView mTextView, final int mStatus) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            // On UI thread.
            if (mStatus == FUNCTION_STATUS_UNCHECKED) {
                mTextView.setText(R.string.welcome_functionUnchecked);
                mTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorWelcome_TableUnchecked));
            } else if (mStatus == FUNCTION_STATUS_UNAVAILABLE) {
                mTextView.setText(R.string.welcome_functionInactive);
                mTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorWelcome_TableUnavailable));
            } else if (mStatus == FUNCTION_STATUS_AVAILABLE) {
                mTextView.setText(R.string.welcome_functionActive);
                mTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorWelcome_TableAvailable));
            }

        } else {
            // Not on UI thread.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setStatus(mTextView, mStatus);
                }
            });
        }

    }

    private void setConnectButton(final Button mScanDevicesButton, final int textResource, final Boolean enabledStatus) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScanDevicesButton.setText(textResource);
                mScanDevicesButton.setEnabled(enabledStatus);
            }
        });
    }

    private void discoverActiveModules(Bluetooth mBluetooth, BluetoothGattService mBluetoothGattService) {
        mBluetooth.readCharacteristic(mBluetoothGattService, mUuidCharacteristicFunctionGen);
        mBluetooth.readCharacteristic(mBluetoothGattService, mUuidCharacteristicMultimeter);
    }
}
