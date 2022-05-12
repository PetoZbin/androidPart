package com.example.orienteering.competition.activeCompetitions;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.competition.competitionPreview.otherPreviews.CompetitorsRecyclerAdapter;
import com.example.orienteering.databinding.FragmentActiveCompetitionsDialogBinding;
import com.example.orienteering.dbWork.registration.UserCompetition;
import com.example.orienteering.helpers.ParseHelper;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;

import java.util.List;


public class ActiveCompetitionsDialog extends AppCompatDialogFragment
        implements View.OnClickListener, ActiveCompetitionsRecyclerAdapter.OnActiveCompetitionClickListener{

    private static final String SERIALIZED_COMPETITION = "serializedCompetition";

    private FragmentActiveCompetitionsDialogBinding binding;
    private ActiveCompetitionsViewModel viewModel;
    private Dialog dialog;

    public ActiveCompetitionsDialog() {
        // Required empty public constructor
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        binding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.fragment_active_competitions_dialog,null,false);
        dialogBuilder.setView(binding.getRoot());

        binding.activeCompsCloseBtn.setOnClickListener(this);
        dialog = dialogBuilder.create();


        viewModel.getDbCompetitions().observe(getViewLifecycleOwner(), new Observer<List<UserCompetition>>() {
            @Override
            public void onChanged(List<UserCompetition> dbCompetitions) {

                if ((dbCompetitions != null) && (!dbCompetitions.isEmpty())){

                    initRecycler();
                }
                else {
                    Toast.makeText(getContext(),getString(R.string.results_empty_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getErrorResponse().observe(getViewLifecycleOwner(), new Observer<CommonResponse>() {
            @Override
            public void onChanged(CommonResponse res) {

                if ((res != null) && (res.getMessage() != null)){
                    Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        viewModel.getCompetitionToPreview().observe(getViewLifecycleOwner(), new Observer<CompetitionOverallData>() {
            @Override
            public void onChanged(CompetitionOverallData comp) {

                if ((comp != null)){

                    moveToCompetitionPreview(comp);
                }
            }
        });

        viewModel.fetchFocusedDbCompetitions();

        return dialog;
    }

    private void moveToCompetitionPreview(CompetitionOverallData competition){

        String serializedCompetition = ParseHelper.serializeCompetition(competition);

        if ((serializedCompetition != null) && (getView() != null)){
            Bundle bundle = new Bundle();
            bundle.putString(SERIALIZED_COMPETITION, serializedCompetition);
            Navigation.findNavController(getView()).navigate(R.id.action_wrappingFragment2_to_compPreviewFragment, bundle);

        }else {

            Log.e("Active comp dialog", "Unable to serialize competition");
            Toast.makeText(getContext(), getString(R.string.results_competition_error), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initRecycler(){

        RecyclerView recycler = binding.activeCompsRecycler;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        ActiveCompetitionsRecyclerAdapter adapter = new ActiveCompetitionsRecyclerAdapter();
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);
        adapter.setItems(viewModel.getDbCompetitions().getValue());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCompetitionClicked(int position) {

        //v recycler view uzivatel klikol na konkretnu sutaz - zobraz prehlad
        //stiahni sutaz zo servera -> observuj vysledok -> serializuj a posli v bundle do preview fragmentu
        viewModel.orderChosenCompToBeFetched(position);
    }

    @Override
    public void onClick(View v) {

        if (v.equals(binding.activeCompsCloseBtn)){
            dialog.cancel();
        }
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