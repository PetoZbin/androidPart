package com.example.orienteering.retrofit.responseClasses.competition;

import com.google.gson.annotations.SerializedName;

public class CompetitionAddResponse {

    @SerializedName("data")
    CompetitionAddData data;

    public CompetitionAddData getData() {
        return data;
    }

    public void setData(CompetitionAddData data) {
        this.data = data;
    }
}
