package com.example.orienteering.retrofit.responseClasses.competition;

import com.example.orienteering.retrofit.patternClasses.CompetitionPattern;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CompetitionOverallResponse extends CommonResponse {

    @SerializedName("data")
    private List<CompetitionOverallData> data;


    public List<CompetitionOverallData> getData() {
        return data;
    }

    public void setData(List<CompetitionOverallData> data) {
        this.data = data;
    }


}
