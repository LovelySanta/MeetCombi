package com.example.laurens.meet_combi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ContentFragment extends Fragment {
    private static final String ARGS_FUNCTION_ID = "functionID";

    public static ContentFragment newInstance(int functionID) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_FUNCTION_ID, functionID);
        ContentFragment fragment;

        switch (functionID) { // Select correct frame

            case R.id.oscilloscope:
                //fragment = new Oscilloscope();
                fragment = new Oscilloscope();
                break;

            case R.id.functionGen:
                fragment = new FunctionGen();
                break;

            case R.id.multimeter:
                fragment = new Multimeter();
                break;

            case R.id.welcome:
                fragment = new Welcome();
                break;

            default:
                fragment = new ContentFragment();
                break;
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parentVG, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_welcome, parentVG, false);
    }
}
