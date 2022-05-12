package com.example.orienteering.competition;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.orienteering.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;


public class TheRunFragment extends com.example.orienteering.tracking.BasicLocationFragment {

    // Vypracovane na zaklade gps kurzu: https://www.youtube.com/watch?v=_xUcYfbtfsI
    // tento fragment sa uz nepouziva


    public TheRunFragment() {
        // Required empty public constructor
    }


    public static TheRunFragment newInstance(String param1, String param2) {
        TheRunFragment fragment = new TheRunFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

        setupLocationCallback();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_the_run, container, false);
    }

    @Override
    public void onLocationProvided(Location location) {
        // TODO implement
    }

    @Override
    public void onLocationProvided(double latitude, double longitude) {

    }

    private void setupGPSUpdates(){



    }



}