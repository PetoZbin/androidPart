package com.example.orienteering.competition.competitionPreview.otherPreviews;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.orienteering.R;
import com.example.orienteering.retrofit.patternClasses.CompetitorPattern;

import java.util.ArrayList;
import java.util.List;

public class CompetitorsRecyclerAdapter extends RecyclerView.Adapter<CompetitorsRecyclerAdapter.CompetitorsListViewHolder> {

    private List<CompetitorPattern> items = new ArrayList<>();

    public void setItems(List<CompetitorPattern> items) {
        this.items = items;
    }

    public static class CompetitorsListViewHolder extends RecyclerView.ViewHolder{

        TextView competitorName;    //competitor nickname
        TextView seqNumber; //iba poradie v zozname - informacne

        public CompetitorsListViewHolder(@NonNull View itemView) {

            super(itemView);
            competitorName = itemView.findViewById(R.id.comp_list_username);
            seqNumber = itemView.findViewById(R.id.comp_list_seq_num_textview);
        }
    }

    @NonNull
    @Override
    public CompetitorsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.basic_competitor_item, parent,false);
        return new CompetitorsRecyclerAdapter.CompetitorsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompetitorsListViewHolder holder, int position) {

        CompetitorPattern competitor = items.get(position);

        try {

            holder.seqNumber.setText(String.valueOf(position+1));
            holder.competitorName.setText(competitor.getUsername());

            if (competitor.getActive()){ //ak uz potvrdil ze je na starte - zeleny
                holder.competitorName.setTextColor(Color.parseColor("#047006"));
            }

        }catch (NullPointerException ex){
            Log.e("competitors list dialog viewholder: ","nullptr - wrong competitor data");
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
