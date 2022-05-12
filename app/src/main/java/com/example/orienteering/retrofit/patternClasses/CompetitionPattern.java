package com.example.orienteering.retrofit.patternClasses;

import com.example.orienteering.competition.CompetitionStates;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CompetitionPattern {

    @SerializedName("name")     //mesto kde sa sutaz odohrava, pripadne uzemny celok
    private String name;

    @SerializedName("municipality")     //mesto kde sa sutaz odohrava, pripadne uzemny celok
    private String municipality;

    @SerializedName("organizerAddress")
    private String organizerAddress;

    @SerializedName("maxCompetitors")   //maximalny mozny pocet zucastnenych
    private int maxCompetitors;

    @SerializedName("status")
    private String status = CompetitionStates.AWAITING.toString();

    @SerializedName("organizerId")
    private String organizerId;

    @SerializedName("metaUrl")  //url metadat
    private String metaUrl;

    @SerializedName("nftId")
    private String nftId;

    @SerializedName("nftName")
    private String nftName;

    @SerializedName("compDateTime")     //("<YYYY-mm-ddTHH:MM:ss>")
    private String compDateTime;

    @SerializedName("durationMins")
    private int durationMins;

    @SerializedName("blockHash")    //identifikator bloku, v ktorom bola zapisana transackia ku smart kontraktu
    private String blockHash;

    @SerializedName("wayPointList")
    private List<CustomWaypointDesc> waypointList;      //CustomWaypointDesc

    public String getOrganizerAddress() {
        return organizerAddress;
    }

    public void setOrganizerAddress(String organizerAddress) {
        this.organizerAddress = organizerAddress;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getMetaUrl() {
        return metaUrl;
    }

    public void setMetaUrl(String metaUrl) {
        this.metaUrl = metaUrl;
    }

    public String getNftId() {
        return nftId;
    }

    public void setNftId(String nftId) {
        this.nftId = nftId;
    }

    public String getCompDateTime() {
        return compDateTime;
    }

    public void setCompDateTime(String compDateTime) {
        this.compDateTime = compDateTime;
    }

    public int getDurationMins() {
        return durationMins;
    }

    public void setDurationMins(int durationMins) {
        this.durationMins = durationMins;
    }

    public List<CustomWaypointDesc> getWaypointList() {
        return waypointList;
    }

    public void setWaypointList(List<CustomWaypointDesc> waypointList) {
        this.waypointList = waypointList;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public int getMaxCompetitors() {
        return maxCompetitors;
    }

    public void setMaxCompetitors(int maxCompetitors) {
        this.maxCompetitors = maxCompetitors;
    }

    public String getNftName() {
        return nftName;
    }

    public void setNftName(String nftName) {
        this.nftName = nftName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LatLng getCheckpointPosBySeqNum(int seqNum){

        for (CustomWaypointDesc waypoint : waypointList){

            if (waypoint.getSeqNumber() == seqNum){

                return new LatLng(waypoint.getLat(), waypoint.getLng());
            }
        }
        return null;
    }

    public CustomWaypointDesc getCheckpointBySeqNum(int seqNum){

        for (CustomWaypointDesc waypoint : waypointList){

            if (waypoint.getSeqNumber() == seqNum){

                return waypoint;
            }
        }
        return null;
    }
}
