package com.example.orienteering.results.detail;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.competition.competitionPreview.otherPreviews.PrizePreviewDialogFragment;
import com.example.orienteering.competition.run.dialogues.finishRecycler.FinishRecyclerAdapter;
import com.example.orienteering.databinding.FragmentResultDetailBinding;
import com.example.orienteering.results.ResultsFragmentViewModel;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionAddData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;


public class ResultDetailFragment extends Fragment implements View.OnClickListener{

    public static final String COMPETITION_ID = "competitionId";

    private FragmentResultDetailBinding binding;
    private ResultDetailViewModel viewModel;
    private FinishRecyclerAdapter recyclerAdapter;

    public ResultDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_result_detail,container,false);
        binding.setLifecycleOwner(this);
        viewModel = new ViewModelProvider(this).get(ResultDetailViewModel.class);
        binding.setResultDetailViewModel(viewModel);

        if ((getArguments() != null) && (getArguments().containsKey(COMPETITION_ID))){

            viewModel.setCompetitionId(getArguments().getString(COMPETITION_ID));
        }

        initRecycler();

        viewModel.getUserId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String userId) {
                if ((userId != null) && (viewModel.getCompetitionId() != null)){

                    viewModel.fetchCompetition(viewModel.getCompetitionId());
                }
                else {
                    Toast.makeText(getContext(),getString(R.string.results_competition_error),Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getCompetition().observe(getViewLifecycleOwner(), new Observer<CompetitionOverallData>() {
            @Override
            public void onChanged(CompetitionOverallData competition) {
                if (competition != null){

                    onCompetitionFetched(competition);
                }
                else {
                    Toast.makeText(getContext(),getString(R.string.results_competition_error),Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getErrorResponse().observe(getViewLifecycleOwner(), new Observer<CommonResponse>() {
            @Override
            public void onChanged(CommonResponse res) {
                if ((res != null) && (res.getMessage() != null)){
                    Toast.makeText(getContext(),res.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });


        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onCompetitionFetched(CompetitionOverallData competition){

        viewModel.setupFieldsData(competition);
        //recycler view s casmi pre jednotlive waypointy
        recyclerAdapter.setItems(competition.prepareRecords(viewModel.getUserId().getValue()));
        recyclerAdapter.notifyDataSetChanged();

    }

    private void initRecycler(){

        RecyclerView recycler = binding.resDetailRecycler;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());  //posielam kontext
        this.recyclerAdapter = new FinishRecyclerAdapter();    // itemlist - list poloziek
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(this.recyclerAdapter);
    }

    @Override
    public void onClick(View v) {

        if (v.equals(binding.resDetailBackBtn)){
            //navrat na fragment basic results
            try {
                getActivity().onBackPressed();
            }catch (Exception ex){
                Log.e("Result detial:"," getActivity onBackPressed nullptr exception");
            }
        }
        else if (v.equals(binding.resDetailShowPrizeBtn)){
            //dialog prize preview
            try {
                PrizePreviewDialogFragment dialog
                        = new PrizePreviewDialogFragment(viewModel.getCompetition().getValue().getNftName(),
                        viewModel.getCompetition().getValue().getMetaUrl());

                dialog.show(getChildFragmentManager(), "quitDialog");

            }catch (NullPointerException ex){
                Log.e("prize preview dialog:","nullptr exception");
            }
        }
    }
}