package com.example.laurens.meet_combi;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class Bluetooth {

    // Activity
    private Activity mActivity;
    private Context mContext;

    public interface Callbacks {
        Bluetooth getBluetooth();
    }

    private Boolean mBusy = false;
    private final String DEBUG_TAG = "Bluetooth";

    // BluetoothAdapter
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 1;
    public static final int REQUEST_LOCATION_PERMISION = 2;

    //BluetoothLeScanner
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    //private static final String DEVICE_ADDRESS = "9C:B6:D0:ED:60:64"; // PC Francis
    private static final String DEVICE_ADDRESS = "B8:27:EB:59:FA:7E";   // RaspberryPi3 from Francis
    private BluetoothDevice mBluetoothDevice;
    public interface ConnectCallbacks {
        void onScanDone(Boolean deviceFound);
        void onConnectingDone(Boolean deviceConnected);
        void onServicesDiscovered();
        void onCharacteristicRead(BluetoothGattCharacteristic characteristicReaded);
    }

    // Handler
    private Handler mHandler;
    private Runnable mHandlerThread;

    // BluetoothGatt
    private BluetoothGatt mBluetoothGatt;
    private Boolean mGattConnected = false;
    private List<BluetoothGattCharacteristic> mCharacteristicsToRead = new ArrayList<>();



    public Bluetooth(Activity activity, Context context) {
        mActivity = activity;
        mContext = context;
    }

    private void init() {
        BluetoothManager mBluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        checkBluetoothSupport();
        checkBLECapabilities();
        requestLocationPermission();
    }
    private void checkBluetoothSupport() {
        // Check if bluetooth is supported on this device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(mActivity, "Bluetooth not supported!", Toast.LENGTH_SHORT).show();
            mActivity.finish();
        }
    }
    private void checkBLECapabilities() {
        // Check if BLE is supported on the device.
        if (!mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mActivity, "BLE not supported!", Toast.LENGTH_SHORT).show();
            mActivity.finish();
        }
    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(mActivity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISION);
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay!
                enable();

            } else {

                // permission denied, boo! Request again!
                requestLocationPermission();

            }
        }
    }

    public void enable() {
        if (mBluetoothAdapter == null) {
            init();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    public void disable() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
        }
    }

    public void scanLeDevice(final ConnectCallbacks connectCallback) {
        // Create scanner
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner == null) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mScanCallback = new ScanCallback() {
                // TODO: possible callback functions: https://developer.android.com/reference/android/bluetooth/le/ScanCallback.html

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    // when scan result found
                    //Log.i("BLE device found", " " + result.getDevice().getAddress());

                    if (result.getDevice().getAddress().equals(DEVICE_ADDRESS)) {
                        mBluetoothDevice = result.getDevice();

                        // Now we can stop scanning
                        mBusy = false;
                        mBluetoothLeScanner.stopScan(mScanCallback);
                        Log.d(DEBUG_TAG+".scanLeDevice", "Found device, stopped scanning.");

                        // Remove terminating thread from handler
                        mHandler.removeCallbacks(mHandlerThread);

                        // Callback
                        connectCallback.onScanDone(true);

                        // Create GATT connection
                        Log.d(DEBUG_TAG+".scanLeDevice", "Creating GATT");
                        createBluetoothGatt(connectCallback);
                    }

                }
            };
        }

        if (!mBusy) {
            if (!mGattConnected) {
                Log.d(DEBUG_TAG + ".scanLeDevice", "Starting to scan for devices.");

                mHandler = new Handler();
                mHandlerThread = new Runnable() {
                    @Override
                    public void run() {
                        if (mBusy) {
                            // Stop scanning if we can't find any matching devices in time
                            mBluetoothLeScanner.stopScan(mScanCallback);
                            mBusy = false;

                            // Report back
                            connectCallback.onScanDone(false);
                        }

                    }
                };
                mHandler.postDelayed(mHandlerThread, 10000);

                mBluetoothLeScanner.startScan(mScanCallback);
                mBusy = true;
            } else {
                // Already connected
                connectCallback.onScanDone(true);
                createBluetoothGatt(connectCallback);
            }
        } else {
            Log.d(DEBUG_TAG+".scanLeDevice", "Bluetooth already scanning");
        }

    }
    private void createBluetoothGatt(final ConnectCallbacks connectCallback) {
        if (!mGattConnected) {
            BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
                // TODO: possible callback functions: https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback.html

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (newState == BluetoothGatt.STATE_CONNECTED) {
                            mGattConnected = true;
                            connectCallback.onConnectingDone(true);

                            // Discover services
                            Log.d(DEBUG_TAG + ".createBluetoothGatt", "Start discovering");
                            discoverServices();
                        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                            mGattConnected = false;
                        }
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Log.d(DEBUG_TAG + ".createBluetoothGatt", "Discovering done!");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        List<BluetoothGattService> mGattServices = gatt.getServices();
                        for (BluetoothGattService mBluetoothGattService : mGattServices) {
                            Log.d(DEBUG_TAG + ".createBluetoothGatt", "Service UUID found: " + mBluetoothGattService.getUuid().toString());
                        }

                        connectCallback.onServicesDiscovered();
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Log.d(DEBUG_TAG + ".readCharacteristic", "Characteristic " + characteristic.getUuid().toString() + " has been read.");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        mCharacteristicsToRead.remove(characteristic);
                        connectCallback.onCharacteristicRead(characteristic);
                    }
                    if (mCharacteristicsToRead.size() > 0) {
                        requestCharacteristicRead();
                    } else {
                        // Done reading
                        mBusy = false;
                    }
                }
            };

            mBluetoothGatt = mBluetoothDevice.connectGatt(
                    mContext,
                    true,
                    mBluetoothGattCallback);
        } else {
            // already connected
            connectCallback.onConnectingDone(true);
            discoverServices();
        }
    }
    public boolean getConnectionStatus() {
        return mGattConnected;
    }

    public void discoverServices() {
        if (!mGattConnected || mBluetoothGatt == null) {
            Log.d(DEBUG_TAG+".discoverServices","BluetoothGatt not connected");
        } else {
            mBluetoothGatt.discoverServices();
        }
    }
    public BluetoothGattService getService(UUID uuid) {
        // https://developer.android.com/reference/android/bluetooth/BluetoothGatt.html#getService(java.util.UUID)
        if (!mGattConnected || mBluetoothGatt == null) {
            Log.d(DEBUG_TAG+".getService","BluetoothGatt not connected");
            return null;
        } else {
            return mBluetoothGatt.getService(uuid);
        }
    }

    private Boolean requestCharacteristicRead() {
        mBusy = mBluetoothGatt.readCharacteristic(mCharacteristicsToRead.get(mCharacteristicsToRead.size() - 1));
        return mBusy;
    }
    private Boolean readCharacteristic(BluetoothGattCharacteristic mBluetoothGattCharacteristic) {
        if (!mGattConnected || mBluetoothGatt == null) {
            Log.d(DEBUG_TAG+".readCharacteristic","BluetoothGatt not connected");
            return false;
        } else if (mBluetoothGattCharacteristic == null) {
            Log.d(DEBUG_TAG+".readCharacteristic", "No characteristic found.");
            return false;
        } else {
            Log.d(DEBUG_TAG + ".readCharacteristic", "Characteristic " + mBluetoothGattCharacteristic.getUuid().toString() + " has been requested to read.");
            mCharacteristicsToRead.add(mBluetoothGattCharacteristic);
            if (mBusy) {
                return true;
            } else {
                // Start reading
                return requestCharacteristicRead();
            }
        }
    }
    public Boolean readCharacteristic(BluetoothGattService mBluetoothGattService, UUID mCharacteristicUuid) {
        // https://developer.android.com/reference/android/bluetooth/BluetoothGattService.html#getCharacteristic(java.util.UUID)
        return readCharacteristic(mBluetoothGattService.getCharacteristic(mCharacteristicUuid));
    }
    public Boolean readCharacteristic(BluetoothGattService mBluetoothGattService, List<UUID> mCharacteristicUuids) {
        Boolean mStatus = false;
        for (UUID mCharacteristicUuid : mCharacteristicUuids) {
            mStatus = readCharacteristic(mBluetoothGattService, mCharacteristicUuid);
        }
        return mStatus;
    }
}
