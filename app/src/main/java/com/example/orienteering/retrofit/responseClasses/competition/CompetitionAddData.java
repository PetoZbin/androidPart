package com.example.orienteering.retrofit.responseClasses.competition;

import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.google.gson.annotations.SerializedName;

public class CompetitionAddData extends CommonResponse {

    @SerializedName("competitionId")
    String competitionId;

    public String getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }
}
