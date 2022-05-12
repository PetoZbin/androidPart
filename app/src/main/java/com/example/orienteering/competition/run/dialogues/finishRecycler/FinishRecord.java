package com.example.orienteering.competition.run.dialogues.finishRecycler;

import java.time.Duration;

public class FinishRecord {

    // trieda pre data polozky finish recycler view

    private String checkpointTime;    //cas
    private String thoroughfare;
    private int seqNum;     //poradove cislo waypointu vramci sutaze

    public String getCheckpointTime() {
        return checkpointTime;
    }

    public void setCheckpointTime(String checkpointTime) {
        this.checkpointTime = checkpointTime;
    }

    public String getThoroughfare() {
        return thoroughfare;
    }

    public void setThoroughfare(String thoroughfare) {
        this.thoroughfare = thoroughfare;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }


    // vypocet casu medzi predoslym a nadchadzajucim wp



}
