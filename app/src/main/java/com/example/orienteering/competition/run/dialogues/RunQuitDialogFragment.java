package com.example.orienteering.competition.run.dialogues;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.orienteering.R;
import com.example.orienteering.competition.CompetitorStates;
import com.example.orienteering.competition.run.OnCompetitionLeftListener;
import com.example.orienteering.databinding.FragmentRunQuitDialogBinding;
import com.example.orienteering.dbWork.registration.UserCompetition;


public class RunQuitDialogFragment extends DialogFragment implements View.OnClickListener{


    private FragmentRunQuitDialogBinding binding;
    private UserCompetition dbComp;
    private Dialog dialog;
    private OnCompetitionLeftListener onCompetitionLeftListener;

    public RunQuitDialogFragment(UserCompetition dbComp) {
        this.dbComp = dbComp;
    }

    public void setOnCompetitionLeftListener(OnCompetitionLeftListener listener) {
        this.onCompetitionLeftListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        binding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_run_quit_dialog,null,false);
        dialogBuilder.setView(binding.getRoot());

        binding.quitProceedBtn.setOnClickListener(this);

        dialog = dialogBuilder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onClick(View v) {

        if (v.equals(binding.quitProceedBtn)){
            dbComp.setActive(false);              //menim stav na joined - moze znova skusit vstupit do sutaze - terajsia implementacia
            dbComp.setCompetitorStatus(CompetitorStates.JOINED.toString()); //stav QUIT sa pouzivav len kvoli zobrazeniu a synchronizacii
            onCompetitionLeftListener.onCompetitionLeft(this.dbComp);
        }
    }

    // percentualne zaplnenie displaya
    @Override
    public void onResume() {
        super.onResume();

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corners);
    }
}