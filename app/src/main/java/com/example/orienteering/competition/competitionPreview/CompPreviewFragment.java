package com.example.orienteering.competition.competitionPreview;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.sax.ElementListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.competition.CompetitionStates;
import com.example.orienteering.competition.CompetitorStates;
import com.example.orienteering.competition.TheRunFragment;
import com.example.orienteering.competition.competitionPreview.otherPreviews.CompetitorsListDialogFragment;
import com.example.orienteering.competition.competitionPreview.otherPreviews.PrizePreviewDialogFragment;
import com.example.orienteering.competition.run.RunActivity;
import com.example.orienteering.databinding.FragmentCompPreviewBinding;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.nfts.nftToCompetition.PickWaypointOptionsDialog;
import com.example.orienteering.nfts.nftToCompetition.compSummary.CompSummaryRecyclerAdapter;
import com.example.orienteering.retrofit.patternClasses.CompetitorPattern;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;
import com.example.orienteering.userAccess.onboarding.LoginViewModel;
import com.example.orienteering.userAccess.onboarding.responses.LoginSuccessResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class CompPreviewFragment extends Fragment implements View.OnClickListener {

    public static final String SERIALIZED_COMPETITION = "serializedCompetition";
    public static final String SERIALIZED_WAYPOINTS = "serializedWaypoints";

    private FragmentCompPreviewBinding previewBinding;
    private CompPreviewViewmodel previewViewmodel;



    public CompPreviewFragment() {
        // Required empty public constructor
    }


    public static CompPreviewFragment newInstance(String param1, String param2) {
        CompPreviewFragment fragment = new CompPreviewFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        previewBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_comp_preview,container,false);
        previewBinding.setLifecycleOwner(this);
        previewViewmodel = new ViewModelProvider(this).get(CompPreviewViewmodel.class);
        previewBinding.setCompPreviewViewModel(previewViewmodel);

        previewBinding.comPreviewBackBtn.setOnClickListener(this);
        previewBinding.comPreviewShowMapBtn.setOnClickListener(this);
        previewBinding.comPreviewShowPrizeBtn.setOnClickListener(this);
        previewBinding.comPreviewCompetitorListBtn.setOnClickListener(this);
        previewBinding.compPreviewJoinBtn.setOnClickListener(this);
        previewBinding.compPreviewGiveUpBtn.setOnClickListener(this);
        previewBinding.compPreviewCancelBtn.setOnClickListener(this);
        previewBinding.compPreviewAtStartBtn.setOnClickListener(this);

        hideActionBarButtons();

        previewViewmodel.getCompetition().observe(getViewLifecycleOwner(), new Observer<CompetitionOverallData>() {
            @Override
            public void onChanged(CompetitionOverallData competition) {
                //sutaz uspesne nacitana - vykresli waypointy do tabulky, vypis informacie

                onCompetitionSet(competition);
                initRecycler(competition.getWaypointList());
            }
        });

        previewViewmodel.getCompetitorResponse().observe(getViewLifecycleOwner(), new Observer<CommonResponse>() {
            @Override
            public void onChanged(CommonResponse response) {

                if (response.getSuccess()){

                    previewBinding.compPreviewJoinBtn.setVisibility(View.GONE);
                    previewViewmodel.loadCompetition();
                    hideActionBarButtons();
                }
                else{

                    Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        previewViewmodel.getCompetitionCancelResponse().observe(getViewLifecycleOwner(), new Observer<CommonResponse>() {
            @Override
            public void onChanged(CommonResponse response) {

                if (response.getSuccess() && (getActivity()!=null)){

                    Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();  //vrat sa na mapu sutazi - sutaz vymazana
                }
            }
        });

        previewViewmodel.getPickedAddress().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String address) {

                if ((getArguments() != null)
                        && (getArguments().containsKey(SERIALIZED_COMPETITION))){
                    previewViewmodel.setCompetition(getArguments().getString(SERIALIZED_COMPETITION));
                }
            }
        });


        previewViewmodel.getAccessCompetitionEvent().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean canAccessCompetition) {

                if (canAccessCompetition){
                    accessCompetition();
                }
            }
        });

        return previewBinding.getRoot();
    }

    private void onCompetitionSet(CompetitionOverallData competition){

        try {

            previewViewmodel.setCompInfoVars(competition);

            if (previewViewmodel.canUserJoin()){

                previewBinding.compPreviewJoinBtn.setVisibility(View.VISIBLE);
                previewBinding.compPreviewGiveUpBtn.setVisibility(View.GONE);

            }
            else {
                previewBinding.compPreviewJoinBtn.setVisibility(View.GONE);
            }

            if (previewViewmodel.canUserCancel()){

                previewBinding.compPreviewCancelBtn.setVisibility(View.VISIBLE);
            }
            else {
                previewBinding.compPreviewCancelBtn.setVisibility(View.GONE);
            }


            if (previewViewmodel.canUserGiveUp()){

                previewBinding.compPreviewGiveUpBtn.setVisibility(View.VISIBLE);
            }
            else {
                previewBinding.compPreviewGiveUpBtn.setVisibility(View.GONE);
            }

            if (previewViewmodel.canUserAttend()){

                previewBinding.compPreviewAtStartBtn.setVisibility(View.VISIBLE);
            }
            else {

                previewBinding.compPreviewAtStartBtn.setVisibility(View.GONE);
            }

        }catch (NullPointerException ex){
            Log.e("Comp preview fragment: ","Null pointer in deserialzied competition");
        }

    }

    private void inactivateBtn(AppCompatButton btn){

        btn.setEnabled(false);
        btn.setAlpha(0.25f);
    }
    private void activateBtn(AppCompatButton btn){

        btn.setEnabled(true);
        btn.setAlpha(1.0f);
    }

    private void initRecycler(List<CustomWaypointDesc> items){

        RecyclerView recycler = previewBinding.compPreviewRecycler;
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        CompSummaryRecyclerAdapter adapter = new CompSummaryRecyclerAdapter();
        adapter.setItemList(items);

        recycler.setAdapter(adapter);
    }

    private void onCompetitorResponse(CommonResponse response){

        if (response.getSuccess()){

            //refreh zoznamu sutaziacich
            Toast.makeText(getContext(), response.getMessage(),Toast.LENGTH_SHORT).show();
        }
        else {

            // notifikuj, ze doslo k chybe - error message
            Toast.makeText(getContext(), response.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void hideActionBarButtons(){

        previewBinding.compPreviewJoinBtn.setVisibility(View.GONE);
        previewBinding.compPreviewAtStartBtn.setVisibility(View.GONE);
        previewBinding.compPreviewGiveUpBtn.setVisibility(View.GONE);
        previewBinding.compPreviewCancelBtn.setVisibility(View.GONE);
    }

    private void accessCompetition(){   //na zaklade eventu - priprava v roomDB - prechod na aktivitu behu

        //String serializedCompetition = new Gson().toJson(previewViewmodel.getCompetition().getValue());
        Intent intent = new Intent(getContext(), RunActivity.class);
        //intent.putExtra(SERIALIZED_COMPETITION, serializedCompetition);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {

        if (v.equals(previewBinding.comPreviewBackBtn)){

            try {
                getActivity().onBackPressed();
            }catch (Exception ex){
                Log.e("Compreview:"," getActivity onBackPressed nullptr exception");
            }
        }
        else if (v.equals(previewBinding.comPreviewShowMapBtn)){

            try {
                List<CustomWaypointDesc> waypoints = previewViewmodel.getCompetition().getValue().getWaypointList();

                String serializedWaypointsDesc = new Gson().toJson(waypoints);

                Bundle bundle = new Bundle();
                bundle.putString(SERIALIZED_WAYPOINTS, serializedWaypointsDesc);
                Navigation.findNavController(v).navigate(R.id.action_compPreviewFragment_to_waypointMapviewFragment, bundle);

            }catch (NullPointerException ex){
                Log.e("Compreview:"," getActivity onBackPressed nullptr exception");
            }

        }
        else if (v.equals(previewBinding.comPreviewCompetitorListBtn)){

            try {


//                CompetitorsListDialogFragment dialog
//                        = new CompetitorsListDialogFragment(comps, previewViewmodel.getCompetitorsNumberString().getValue());

                CompetitorsListDialogFragment dialog
                        = new CompetitorsListDialogFragment(previewViewmodel.getCompetition().getValue().getCompetitorsList(),
                                previewViewmodel.getCompetitorsNumberString().getValue());

                dialog.setTargetFragment(this, 0);
                dialog.show(getFragmentManager().beginTransaction(), "wayopt");

            }catch (NullPointerException ex){
                Log.e("Compreview competitors dialog :"," nullptr exception");
            }
        }
        else if (v.equals(previewBinding.comPreviewShowPrizeBtn)){

            try {
                PrizePreviewDialogFragment dialog
                        = new PrizePreviewDialogFragment(previewViewmodel.getCompetition().getValue().getNftName(),
                        previewViewmodel.getCompetition().getValue().getMetaUrl());

                dialog.setTargetFragment(this, 1);
                dialog.show(getFragmentManager().beginTransaction(), "wayopt");

            }catch (NullPointerException ex){
                Log.e("prize preview dialog:","nullptr exception");
            }
        }
        else if (v.equals(previewBinding.compPreviewJoinBtn)){

            previewViewmodel.joinCompetition();
        }
        else if (v.equals(previewBinding.compPreviewGiveUpBtn) && getContext() !=null){

            try {
                previewViewmodel.giveUpCompetition();
            }catch (NullPointerException ex){
                Toast.makeText(getContext(), getContext().getString(R.string.db_user_fetch_err), Toast.LENGTH_SHORT).show();
            }

        }
        else if (v.equals(previewBinding.compPreviewCancelBtn)){

            previewViewmodel.cancelCompetition();
        }
        else if (v.equals(previewBinding.compPreviewAtStartBtn)){       //prepne na sutaznu aktivitu

            // je na starte - tlacidlo sa objavi 2 min do startu
            // prepne sa aktivita uzivatel pozera na odpocitavanie a caka


            try {

                CompetitionOverallData competition = previewViewmodel.getCompetition().getValue();

                // prihlasit sa da len do sutaze, ktora prebieha (nie do este nezacatej / skoncenej)
                if (competition.getStatus().equals(CompetitionStates.ONGOING.toString())){

                    previewViewmodel.onToCompetitionBtn(competition.getCompetitionId());
                }
                else {

                    if (competition.getStatus().equals(CompetitionStates.AWAITING.toString())){

                        //refresh
                        Toast.makeText(getContext(), "Copetition not yet started. Please wait.", Toast.LENGTH_SHORT).show();
                        previewViewmodel.loadCompetition();
                    }
                }



            }catch (NullPointerException ex){

                Log.e("competition fetch", "NUll ptr when fetching competition instance for parse");
            }
        }

    }


}