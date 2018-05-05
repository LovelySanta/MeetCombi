package com.example.laurens.meet_combi;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements Navigator.Callbacks, Welcome.Callbacks {

    private Bluetooth mBluetooth;
    private Navigator mNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FragmentManager maintains a list of fragments
        FragmentManager fm = getSupportFragmentManager();

        // Navigation view
        Fragment fragment = fm.findFragmentById(R.id.navigation_frame);
        if (fragment == null) {
            mNavigator = new Navigator(); // Navigator fragment
            fragment = mNavigator;

            fm.beginTransaction()
                    .add(R.id.navigation_frame, fragment)
                    .commit();
        }

        // Welcome view
        fragment = fm.findFragmentById(R.id.content_frame);
        if (fragment == null) {
            fragment = new Welcome(); // Welcome fragment

            fm.beginTransaction()
                    .add(R.id.content_frame, fragment)
                    .commit();
        }

        // Fixed orientation
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Bluetooth
        mBluetooth = new Bluetooth(this, getApplicationContext());
        //mBluetooth.init();
        mBluetooth.enable();
        //mBluetooth.scanLeDevice();


        //mBluetooth.disable();



    }

    public void onFunctionSelected(int functionID) {
        Fragment newContentFragment = ContentFragment.newInstance(functionID);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, newContentFragment)
                .commit();

        DrawerLayout dl = findViewById(R.id.drawer_layout);
        dl.closeDrawer(GravityCompat.START);
    }

    public Bluetooth getBluetooth() {
        return mBluetooth;
    }

    public void enableFunction(int functionID, Boolean functionEnabled) {
        mNavigator.enableFunction(functionID, functionEnabled);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        // Relay to Bluetooth if available
        if (mBluetooth != null) {
            mBluetooth.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
