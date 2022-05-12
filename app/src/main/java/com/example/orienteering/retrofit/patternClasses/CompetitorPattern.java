package com.example.orienteering.retrofit.patternClasses;

import com.google.gson.annotations.SerializedName;

public class CompetitorPattern {

//    @SerializedName("id")
//    private Integer id;

    @SerializedName("userId")
    private String competitorId;

    @SerializedName("competitionId")
    private String competitionId;

    @SerializedName("username")
    private String username;

    @SerializedName("competitorEthAddress")
    private String competitorEthAddress;

    @SerializedName("active")
    private Integer active;

    @SerializedName("competitorStatus")
    private String status;

    @SerializedName("timeStartFinish")    //cas za ktory prebehol od startu do ciela
    private String timeStartFinish;

    @SerializedName("totalTime")        //celkovy cas start ciel - novy sposob - server
    private String totalTime;

    @SerializedName("totalRank")        //celkove poradie v cieli
    private String totalRank;

    public String getCompetitorId() {
        return competitorId;
    }

    public void setCompetitorId(String competitorId) {
        this.competitorId = competitorId;
    }

    public String getCompetitorEthAddress() {
        return competitorEthAddress;
    }

    public void setCompetitorEthAddress(String competitorEthAddress) {
        this.competitorEthAddress = competitorEthAddress;

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getActive() {
        return (this.active > 0);
    }

    public void setActive(Boolean confirmed) {
        this.active = confirmed ? 1:0;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }

    public String getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }

    public String getTimeStartFinish() {
        return timeStartFinish;
    }

    public void setTimeStartFinish(String timeStartFinish) {
        this.timeStartFinish = timeStartFinish;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public String getTotalTime() {

        if (this.totalTime == null){

            return "-";

        }else if (this.totalTime.equals("")){

            return "-";

        }else {
            return totalTime;
        }
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getTotalRank() {

        if (this.totalRank == null){

            return "-";

        }else if (this.totalRank.equals("")){

            return "-";

        }else {
            return this.totalRank;
        }
    }

    public void setTotalRank(String totalRank) {
        this.totalRank = totalRank;
    }


}
