package com.example.orienteering.nfts.nftToCompetition;

import com.example.orienteering.retrofit.patternClasses.LeaderBoardItemPattern;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CustomWaypointDesc {

    @SerializedName("waypointId")
    private String id;

    @SerializedName("competitionId")
    String competitionId;

    @SerializedName("lat")
    private Double lat;

    @SerializedName("lng")
    private Double lng;

    @SerializedName("seqNumber")
    private int seqNumber;

    @SerializedName("municipality")     //uzemny celok - na ucely ulozenie waypointu a jeho vuhladavanie v db
    private String municipality;

    @SerializedName("thoroughfare")
    private String thoroughfare;   // adresa - pomenovanie bodu z geocoder
// poradie ucastnikov na danom waypointe

    @SerializedName("isFinish")
    private int isFinish;

    @SerializedName("leaderboard")
    private List<LeaderBoardItemPattern> leaderboard = new ArrayList<>();

    public List<LeaderBoardItemPattern> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<LeaderBoardItemPattern> leaderboard) {
        this.leaderboard = leaderboard;
    }


    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    public String getThoroughfare() {
        return thoroughfare;
    }

    public void setThoroughfare(String thoroughfare) {
        this.thoroughfare = thoroughfare;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }

    public int getIsFinish() {
        return isFinish;
    }

    public void setIsFinish(int isFinish) {
        this.isFinish = isFinish;
    }

    public Boolean isFinish(){

        return (this.isFinish > 0);
    }

    public void setIsFinish(Boolean isFinish){

        this.isFinish = isFinish? 1:0;
    }


    public LeaderBoardItemPattern getLeaderBoardRecordByCompetitorId(String competitorId){

        for (LeaderBoardItemPattern record : leaderboard){

            if (record.getCompetitorId().equals(competitorId)){ return record; }

        }
        return null;
    }

}
