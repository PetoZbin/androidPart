package com.example.orienteering.competition.competitionPreview.otherPreviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentCompetitorsListDialogBinding;
import com.example.orienteering.nfts.nftToCompetition.compSummary.summaryDialog.CompSummaryDialogViewModel;
import com.example.orienteering.retrofit.patternClasses.CompetitorPattern;
import com.example.orienteering.userAccess.accountPicker.pickerRecycler.PickerRecyclerAdapter;

import java.util.List;
import java.util.Objects;


public class CompetitorsListDialogFragment extends AppCompatDialogFragment implements View.OnClickListener {


    private List<CompetitorPattern> competitors;
    private String maxCompetitors = "";

    private FragmentCompetitorsListDialogBinding binding;
    private Dialog dialog;

    public CompetitorsListDialogFragment(List<CompetitorPattern> competitors, String maxCompetitors) {
        this.competitors = competitors;
        this.maxCompetitors = maxCompetitors;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        binding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_competitors_list_dialog,null,false);
        dialogBuilder.setView(binding.getRoot());

        binding.compListCloseBtn.setOnClickListener(this);

        initRecycler();

        binding.compListCompLimitText.setText(maxCompetitors);

        dialog = dialogBuilder.create();
        return dialog;
    }

    private void initRecycler(){

        RecyclerView recycler = binding.compListRecycler;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());  //posielam kontext
        CompetitorsRecyclerAdapter adapter = new CompetitorsRecyclerAdapter();    // itemlist - list poloziek
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);
        adapter.setItems(competitors);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }



    @Override
    public void onClick(View v) {

        if (v.equals(binding.compListCloseBtn)){
            dialog.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();


        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.80);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.70);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corners);
    }
}