package com.example.orienteering.competition.activeCompetitions;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orienteering.R;
import com.example.orienteering.competition.CompetitionStates;
import com.example.orienteering.competition.CompetitorStates;
import com.example.orienteering.competition.competitionPreview.otherPreviews.CompetitorsRecyclerAdapter;
import com.example.orienteering.dbWork.registration.UserCompetition;
import com.example.orienteering.retrofit.patternClasses.CompetitorPattern;
import com.example.orienteering.userAccess.accountPicker.pickerRecycler.PickerRecyclerAdapter;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ActiveCompetitionsRecyclerAdapter
        extends RecyclerView.Adapter<ActiveCompetitionsRecyclerAdapter.ActiveCompetitionsViewHolder>{

    private List<UserCompetition> items = new ArrayList<>();
    private OnActiveCompetitionClickListener onCompetitionClickListener;

    public void setItems(List<UserCompetition> items) {
        this.items = items;
    }

    public void setOnCompetitionClickListener(OnActiveCompetitionClickListener onCompetitionClickListener) {
        this.onCompetitionClickListener = onCompetitionClickListener;
    }

    public static class ActiveCompetitionsViewHolder extends RecyclerView.ViewHolder{

        TextView competitionName;
        TextView ownerStateText; //iba poradie v zozname - informacne
        TextView competitorStateText;

        public ActiveCompetitionsViewHolder(@NonNull View itemView, final OnActiveCompetitionClickListener compClickListener) {

            super(itemView);
            competitionName = itemView.findViewById(R.id.active_comp_item_competition_name);

            competitionName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (compClickListener != null){

                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION){

                            compClickListener.onCompetitionClicked(position);
                        }
                    }
                }
            });
            ownerStateText = itemView.findViewById(R.id.active_comp_item_owner_text);
            competitorStateText = itemView.findViewById(R.id.active_comp_item_competitor_state);
        }

    }


    @NonNull
    @Override
    public ActiveCompetitionsRecyclerAdapter.ActiveCompetitionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.active_competition_item, parent,false);
        return new ActiveCompetitionsRecyclerAdapter.ActiveCompetitionsViewHolder(view, this.onCompetitionClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ActiveCompetitionsRecyclerAdapter.ActiveCompetitionsViewHolder holder, int position) {

        UserCompetition competition = items.get(position);

        try {

            if (competition.getCompetitionName() != null){

                holder.competitionName.setText(competition.getCompetitionName());
            }


            if ((competition.isOwner != null) && competition.isOwner){
                holder.ownerStateText.setVisibility(View.VISIBLE);
            }else {
                holder.ownerStateText.setVisibility(View.INVISIBLE);
            }

            if (competition.getCompetitorStatus() != null){

                assignCompetitorState(competition.getCompetitorStatus(), holder.competitorStateText);
            }


        }catch (NullPointerException ex){
            Log.e("competitors list dialog viewholder: ","nullptr - wrong competitor data");
        }

    }

    private void assignCompetitorState(String competitorStatus, TextView compStateView){

        compStateView.setVisibility(View.VISIBLE);

        if (competitorStatus.equalsIgnoreCase(CompetitorStates.PERFORMING.toString())){

            compStateView.setText(R.string.user_state_performing);
        }
        else if (competitorStatus.equalsIgnoreCase(CompetitorStates.ONBOARDING.toString())){

            compStateView.setText(R.string.user_state_onboarding);

        }else if(competitorStatus.equalsIgnoreCase(CompetitorStates.QUIT.toString())){

            compStateView.setText(R.string.user_state_quit);

        }else if (competitorStatus.equalsIgnoreCase(CompetitorStates.JOINED.toString())){

            compStateView.setText(R.string.user_state_joined);
        }else {

            compStateView.setVisibility(View.INVISIBLE);
        }
    }



    @Override
    public int getItemCount() {
        return items.size();
    }



   public interface OnActiveCompetitionClickListener{

        void onCompetitionClicked(int position);
    }

}
