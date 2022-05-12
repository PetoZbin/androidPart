package com.example.orienteering.retrofit.patternClasses;

import com.example.orienteering.helpers.DateHelper;
import com.google.gson.annotations.SerializedName;



public class BasicResultPattern {

    @SerializedName("competitionId")
    private String competitionId;

    @SerializedName("name")
    private String competitionName;

    @SerializedName("compDateTime")
    private String compDateTime;

    @SerializedName("maxCompetitors")
    private Integer maxCompetitors;

    @SerializedName("numCompetitors")
    private Integer numCompetitors;

    @SerializedName("userId")
    private String userId;

    @SerializedName("totalTime")
    private String totalTime;

    @SerializedName("totalRank")
    private Integer totalRank;

    public String getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompDateTime() {
        return compDateTime;
    }

    public void setCompDateTime(String compDateTime) {
        this.compDateTime = compDateTime;
    }

    public Integer getMaxCompetitors() {
        return maxCompetitors;
    }

    public void setMaxCompetitors(Integer maxCompetitors) {
        this.maxCompetitors = maxCompetitors;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public Integer getTotalRank() {
        return totalRank;
    }

    public void setTotalRank(Integer totalRank) {
        this.totalRank = totalRank;
    }

    public Integer getNumCompetitors() {
        return numCompetitors;
    }

    public void setNumCompetitors(Integer numCompetitors) {
        this.numCompetitors = numCompetitors;
    }

    public String getSkCompetitionDateTime(){

        return DateHelper.convertIsoToSk(this.compDateTime);
    }
}
