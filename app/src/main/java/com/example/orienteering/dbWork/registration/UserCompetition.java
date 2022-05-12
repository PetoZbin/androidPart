package com.example.orienteering.dbWork.registration;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.orienteering.competition.CompetitorStates;

import java.util.Date;

import jnr.ffi.annotations.In;

@Entity(tableName = "user_competition", indices = {

        //@Index(value = "competitionId", unique = true)
       // @Index(value = "address", unique = true)
})
public class UserCompetition {


    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "userId")     //referencia na uzivatela - cudzi kluc
    public String userId;

    @ColumnInfo(name = "address")       //eth address
    public String address;

    @ColumnInfo(name = "competitionId")
    public String competitionId;

    @ColumnInfo(name = "competitionName")
    public String competitionName;

    @ColumnInfo(name = "active")
    public Integer active;

    @ColumnInfo(name = "competitionStatus") //finalized/ongoing/awaiting/canceled
    public String competitionStatus;

    @ColumnInfo(name = "competitorStatus")  //joined / PERFORMING/ quit/
    public String competitorStatus = CompetitorStates.SPECTATOR.toString();

    @ColumnInfo(name = "lastCheckpointPos") //poradove cislo posledneho uz dosiahnuteho checkpointu
    public Integer lastCheckpointPos;

    @ColumnInfo(name = "nextWaypointId") //id dosahovaneho waypointu
    public String nextWaypointId;

    @ColumnInfo(name = "nextCheckpointLat")
    public Double nextCheckpointLat;

    @ColumnInfo(name = "nextCheckpointLng")
    public Double nextCheckpointLng;

    @ColumnInfo(name = "isOwner")   //ci je organizator
    public Boolean isOwner;

    @ColumnInfo(name = "compDateTime")
    public String CompDateTime;

    @ColumnInfo(name = "durationMins")
    public Integer durationMins;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }

    public String getCompetitionStatus() {
        return competitionStatus;
    }

    public void setCompetitionStatus(String competitionStatus) {
        this.competitionStatus = competitionStatus;
    }

    public String getCompetitorStatus() {
        return competitorStatus;
    }

    public void setCompetitorStatus(String competitorStatus) {
        this.competitorStatus = competitorStatus;
    }

    public Boolean getOwner() {
        return isOwner;
    }

    public void setOwner(Boolean owner) {
        isOwner = owner;
    }

    public Integer getLastCheckpointPos() {
        return lastCheckpointPos;
    }

    public void setLastCheckpointPos(Integer lastCheckpointPos) {
        this.lastCheckpointPos = lastCheckpointPos;
    }

    public Double getNextCheckpointLat() {
        return nextCheckpointLat;
    }

    public void setNextCheckpointLat(Double nextCheckpointLat) {
        this.nextCheckpointLat = nextCheckpointLat;
    }

    public Double getNextCheckpointLng() {
        return nextCheckpointLng;
    }

    public void setNextCheckpointLng(Double nextCheckpointLng) {
        this.nextCheckpointLng = nextCheckpointLng;
    }

    public String getCompDateTime() {
        return CompDateTime;
    }

    public void setCompDateTime(String compDateTime) {
        CompDateTime = compDateTime;
    }

    public Integer getDurationMins() {
        return durationMins;
    }

    public void setDurationMins(Integer durationMins) {
        this.durationMins = durationMins;
    }


    public String getNextWaypointId() {
        return nextWaypointId;
    }

    public void setNextWaypointId(String nextWaypointId) {
        this.nextWaypointId = nextWaypointId;
    }

    public Boolean getActive() {
        return (active > 0);
    }

    public void setActive(Boolean active) {
        this.active = active? 1:0;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }
}
