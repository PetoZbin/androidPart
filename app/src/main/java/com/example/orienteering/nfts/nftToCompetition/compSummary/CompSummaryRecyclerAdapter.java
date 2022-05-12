package com.example.orienteering.nfts.nftToCompetition.compSummary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orienteering.R;
import com.example.orienteering.databinding.AdressItemBinding;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.userAccess.accountPicker.pickerRecycler.PickerRecyclerAdapter;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CompSummaryRecyclerAdapter extends RecyclerView.Adapter<CompSummaryRecyclerAdapter.CompSummaryViewHolder> {

    List<CustomWaypointDesc> itemList = new ArrayList<>();

    public void setItemList(List<CustomWaypointDesc> itemList) {
        this.itemList = itemList;
    }

    public static class CompSummaryViewHolder extends RecyclerView.ViewHolder{

        TextView throughfare;
        TextView latView;
        TextView longView;
        TextView sequenceNumView;


        public CompSummaryViewHolder(@NonNull View itemView) {

            super(itemView);

            throughfare = itemView.findViewById(R.id.way_des_item_thoroughfare);
            latView = itemView.findViewById(R.id.way_desc_lat);
            longView = itemView.findViewById(R.id.way_desc_lng);
            sequenceNumView = itemView.findViewById(R.id.way_desc_seq_num_textview);

        }
    }

    @NonNull
    @Override
    public CompSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.waypoint_desc_item, parent,false);
        return new CompSummaryRecyclerAdapter.CompSummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompSummaryViewHolder holder, int position) {

        holder.sequenceNumView.setText(String.valueOf(itemList.get(position).getSeqNumber()));

        if (position == 0){

            // prvy cierny
            holder.sequenceNumView.getBackground().setTint(holder.itemView.getResources().getColor(R.color.black));
        }

        if (position == (itemList.size() -1)){

            holder.sequenceNumView.getBackground().setTint(holder.itemView.getResources().getColor(R.color.continue_green));
        }

        holder.throughfare.setText(itemList.get(position).getThoroughfare());
        holder.latView.setText(String.valueOf(itemList.get(position).getLat()));
        holder.longView.setText(String.valueOf(itemList.get(position).getLng()));

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
