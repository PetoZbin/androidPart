package com.example.orienteering.nfts.nftToCompetition;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.orienteering.R;


public class PickWaypointOptionsDialog extends AppCompatDialogFragment implements View.OnClickListener{

    private CustomWaypointDesc waypointDesc;

    private ImageButton deleteBtn;
    private TextView thoroughfareView;
    private TextView latView;
    private TextView lngView;
    private TextView seqNumView;

    private Dialog dialog;

    private OnDeleteListener listener;

    public PickWaypointOptionsDialog(CustomWaypointDesc waypointDesc) {

        this.waypointDesc = waypointDesc;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_pick_waypoit_options, null);

        deleteBtn = view.findViewById(R.id.pick_way_delete);
        deleteBtn.setOnClickListener(this);

        initTextViews(view);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();


        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onClick(View v) {

        if (v.equals(deleteBtn)){
            listener.onWayPointDelete(waypointDesc.getLat(), waypointDesc.getLng());
            dialog.cancel();
        }

    }

    public void setDeleteListener(OnDeleteListener listener) {
        this.listener = listener;
    }

    private void initTextViews(View view){

        thoroughfareView = view.findViewById(R.id.pick_way_address);
        latView = view.findViewById(R.id.way_opt_lat_content);
        lngView = view.findViewById(R.id.way_opt_long_content);
        seqNumView = view.findViewById(R.id.way_opt_seq_content);

        //bundle set
        thoroughfareView.setText(waypointDesc.getThoroughfare());
        latView.setText(String.valueOf(waypointDesc.getLat()));
        lngView.setText(String.valueOf(waypointDesc.getLng()));
        seqNumView.setText(String.valueOf(waypointDesc.getSeqNumber()));

        if (waypointDesc.getSeqNumber() == 1){

            seqNumView.getBackground().setTint(Color.BLACK);
        }
    }

    public interface OnDeleteListener{

        void onWayPointDelete(double latitude, double longitude);
    }


    @Override
    public void onResume() {
        super.onResume();


        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.80);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corners);
    }
}