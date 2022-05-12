package com.example.orienteering.competition.run.dialogues;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.media.DeniedByServerException;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.competition.CompetitorStates;
import com.example.orienteering.competition.competitionPreview.otherPreviews.CompetitorsRecyclerAdapter;
import com.example.orienteering.competition.run.OnCompetitionLeftListener;
import com.example.orienteering.competition.run.dialogues.finishRecycler.FinishRecord;
import com.example.orienteering.competition.run.dialogues.finishRecycler.FinishRecyclerAdapter;
import com.example.orienteering.databinding.FragmentRunFinishDialogueBinding;
import com.example.orienteering.dbWork.registration.UserCompetition;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.nfts.nftToCompetition.compSummary.summaryDialog.CompApproveDialogFragment;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RunFinishDialogueFragment extends DialogFragment implements View.OnClickListener {


    private UserCompetition dbCompetition;  //internal db
    private CompetitionOverallData competition; //server
    private Dialog dialog;
    private FinishRecyclerAdapter adapter;
    private OnCompetitionLeftListener onCompetitionLeftListener;

    private FragmentRunFinishDialogueBinding binding;

    public RunFinishDialogueFragment(UserCompetition dbCompetition) {
        this.dbCompetition = dbCompetition;
    }

    public void setOnCompetitionLeftListener(OnCompetitionLeftListener onCompetitionLeftListener) {
        this.onCompetitionLeftListener = onCompetitionLeftListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        binding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_run_finish_dialogue,null,false);
        dialogBuilder.setView(binding.getRoot());
        binding.finishProceedBtn.setOnClickListener(this);

        initRecycler();
        fetchCompetitionServer();

        dialog = dialogBuilder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private void initRecycler(){

        RecyclerView recycler = binding.finishWpRecycler;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());  //posielam kontext
        this.adapter = new FinishRecyclerAdapter();    // itemlist - list poloziek
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(this.adapter);
//        adapter.setItems(competitors);
//        adapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {

        if (v.equals(binding.finishProceedBtn)){
            //zrus dialog, ukonci aktivitu
            dbCompetition.setActive(false);
            onCompetitionLeftListener.onCompetitionLeft(dbCompetition);
            dialog.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_corners);
    }

    private List<FinishRecord> prepareRecords(CompetitionOverallData competition){

        //pripravy polozky recycler view - thoroughfare + waypoint sequence + cas od zaciatku po dany wp

        List<FinishRecord> resultList = new ArrayList<>();

        for (CustomWaypointDesc waypoint : competition.getWaypointList()){

            waypoint.getLeaderBoardRecordByCompetitorId(dbCompetition.getUserId());
            FinishRecord fr = new FinishRecord();
            fr.setThoroughfare(waypoint.getThoroughfare());
            fr.setSeqNum(waypoint.getSeqNumber());
            fr.setCheckpointTime(competition
                    .durationTimeBetweenWaypoints(1,waypoint.getSeqNumber(),dbCompetition.getUserId()));
            resultList.add(fr);
        }

        return resultList;
    }

    private void initBasicInfo(CompetitionOverallData competition){



        try {

            int lastWaypointSeqNum = competition.getWaypointList().size();

            binding.finishDialTime
                    .setText(competition.durationTimeBetweenWaypoints(1, lastWaypointSeqNum, dbCompetition.userId));

            binding.finishDialStanding.setText(String.valueOf(competition.resolveCompetitorStanding(lastWaypointSeqNum,dbCompetition.userId)));

            String compEndTime = "";

            compEndTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                                                        .format(competition.getCompetitionEndDate());
            binding.finishHandover.setText(compEndTime);

        }catch (ParseException ex){
            Log.e("competition end date count: ","date parse exception");
        }catch (NullPointerException ex){
            Toast.makeText(getContext(),getString(R.string.malformed_competition_response), Toast.LENGTH_SHORT).show();
            Log.e("competition end date count: ","date parse exception");
        }



    }

    private void fetchCompetitionServer(){

        // dotiahni prislusnu sutaz zo servera - retrofit

        try {

            ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            Call<CompetitionOverallResponse> call = apiInterface.getCompetitionById(dbCompetition.competitionId);

            call.enqueue(new Callback<CompetitionOverallResponse>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(Call<CompetitionOverallResponse> call, Response<CompetitionOverallResponse> response) {

                    if (!response.isSuccessful()){

                        try {
                            Log.e("cannot fetch actual comp state err message: ",response.errorBody().string());
                            Toast.makeText(getContext(),getString(R.string.run_finish_dialog_comp_fetch_err),Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            Log.e("cannot fetch actual comp state err message: ","Cannot fetch!");
                            Toast.makeText(getContext(),getString(R.string.run_finish_dialog_comp_fetch_err),Toast.LENGTH_LONG).show();
                        }
                    }
                    else if(response.code() == 200){

                        Log.d("message", response.message());

                        CompetitionOverallResponse res = response.body();

                        if (!res.getData().isEmpty()){

                            CompetitionOverallData serverComp = res.getData().get(0);

                            initBasicInfo(serverComp);
                            List <FinishRecord> records = prepareRecords(serverComp);
                            adapter.setItems(records);
                            adapter.notifyDataSetChanged();
                        }


                    }
                }

                @Override
                public void onFailure(Call<CompetitionOverallResponse> call, Throwable t) {

                    //chyba spojenia toast
                    Log.d("connection error", "smt went wrong");
                    Toast.makeText(getContext(),getString(R.string.run_finish_dialog_comp_fetch_err),Toast.LENGTH_LONG).show();
                }
            });



        }catch (NullPointerException ex){

            Toast.makeText(getContext(),getString(R.string.run_finish_dialog_comp_fetch_err),Toast.LENGTH_LONG).show();
        }

    }

}