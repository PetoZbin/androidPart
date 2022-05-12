package com.example.orienteering.competition.run.dialogues.finishRecycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orienteering.R;
import com.example.orienteering.nfts.nftToCompetition.compSummary.CompSummaryRecyclerAdapter;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;

import java.util.ArrayList;
import java.util.List;

public class FinishRecyclerAdapter extends RecyclerView.Adapter<FinishRecyclerAdapter.FinishViewHolder>{

    private List<FinishRecord> items = new ArrayList<>();

    public static class FinishViewHolder extends RecyclerView.ViewHolder{

        TextView throughfareView;
        TextView timeView;;
        TextView sequenceNumView;


        public FinishViewHolder(@NonNull View itemView) {

            super(itemView);

            throughfareView = itemView.findViewById(R.id.finish_item_thoroughfare);
            timeView = itemView.findViewById(R.id.finish_item_time);
            sequenceNumView = itemView.findViewById(R.id.finish_item_seq_num_textview);

        }
    }

    public void setItems(List<FinishRecord> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public FinishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.finish_record_item, parent,false);
        return new FinishRecyclerAdapter.FinishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FinishViewHolder holder, int position) {

        if (position == 0){
            // prvy cierny
            holder.sequenceNumView.getBackground().setTint(holder.itemView.getResources().getColor(R.color.black));
        }

        if (position == (items.size() -1)){
            holder.sequenceNumView.getBackground().setTint(holder.itemView.getResources().getColor(R.color.continue_green));
        }

        FinishRecord record = items.get(position);

        holder.timeView.setText(record.getCheckpointTime().toString());
        holder.sequenceNumView.setText(String.valueOf(record.getSeqNum()));
        holder.throughfareView.setText(record.getThoroughfare());
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

}
