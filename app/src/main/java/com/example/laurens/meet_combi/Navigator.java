package com.example.laurens.meet_combi;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class Navigator extends Fragment {

    // Navigator buttons
    private Button mWelcome;
    private Button mOsciloscope;
    private Button mFunctionGen;
    private Button mMultimeter;

    private Callbacks mCallbacks;
    public interface Callbacks {
        void onFunctionSelected(int functionID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parentVG, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_navigator, parentVG, false);

        mWelcome = (Button) v.findViewById(R.id.navigatorbutton_welcome);
        mWelcome.setText(R.string.navigator_welcome);
        mWelcome.setEnabled(true);
        mWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Oscilloscope navigation button pressed
                mCallbacks.onFunctionSelected(R.id.welcome);
            }
        });

        mOsciloscope = (Button) v.findViewById(R.id.navigatorbutton_oscilloscope);
        mOsciloscope.setText(R.string.navigator_oscilloscoop);
        mOsciloscope.setEnabled(false);
        mOsciloscope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Oscilloscope navigation button pressed
                mCallbacks.onFunctionSelected(R.id.oscilloscope);
            }
        });

        mFunctionGen = (Button) v.findViewById(R.id.navigatorbutton_functionGen);
        mFunctionGen.setText(R.string.navigator_functieGen);
        mFunctionGen.setEnabled(false);
        mFunctionGen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Oscilloscope navigation button pressed
                mCallbacks.onFunctionSelected(R.id.functionGen);
            }
        });

        mMultimeter = (Button) v.findViewById(R.id.navigatorbutton_multimeter);
        mMultimeter.setText(R.string.navigator_multimeter);
        mMultimeter.setEnabled(false);
        mMultimeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Oscilloscope navigation button pressed
                mCallbacks.onFunctionSelected(R.id.multimeter);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void enableFunction(final int functionID, final Boolean functionEnabled) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (functionID == R.id.oscilloscope) {
                    mOsciloscope.setEnabled(functionEnabled);
                }

                else if (functionID == R.id.functionGen) {
                    mFunctionGen.setEnabled(functionEnabled);
                }

                else if (functionID == R.id.multimeter) {
                    mMultimeter.setEnabled(functionEnabled);
                }
            }
        });
    }
}
