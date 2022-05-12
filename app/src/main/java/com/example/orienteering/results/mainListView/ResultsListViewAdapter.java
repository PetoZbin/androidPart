package com.example.orienteering.results.mainListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.orienteering.R;
import com.example.orienteering.retrofit.patternClasses.BasicResultPattern;

import java.util.List;

public class ResultsListViewAdapter extends ArrayAdapter<BasicResultPattern> {


    public ResultsListViewAdapter(@NonNull Context context, @NonNull List<BasicResultPattern> objects) {
        super(context, R.layout.basis_result_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        BasicResultPattern resultItem = getItem(position);

        if (convertView == null){

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.basis_result_item, parent, false);
        }

        TextView compNameText = convertView.findViewById(R.id.basic_record_comp_name);
        TextView compDateTimeText = convertView.findViewById(R.id.basic_record_item_comp_date);
        TextView standingText = convertView.findViewById(R.id.basic_result_item_standing);
        TextView numCompetitorsText = convertView.findViewById(R.id.basic_result_item_numcompetitors);
        TextView achievedTimeText = convertView.findViewById(R.id.basic_result_item_time);


        compNameText.setText(resultItem.getCompetitionName());
        compDateTimeText.setText(resultItem.getSkCompetitionDateTime());

        if (resultItem.getTotalRank() != null){
            standingText.setText(String.valueOf(resultItem.getTotalRank()));
        }

        if (resultItem.getNumCompetitors() != null){
            numCompetitorsText.setText(String.valueOf(resultItem.getNumCompetitors()));
        }

        achievedTimeText.setText(resultItem.getTotalTime());


        return convertView;
    }
}
