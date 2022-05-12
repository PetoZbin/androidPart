package com.example.orienteering.retrofit.patternClasses;

import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LeaderboardWaypoint extends CustomWaypointDesc {

    // waypoint, ktory ma zoznam objektov sutaziacich - tzv leaderboard

    @SerializedName("leaderboard")
    List<LeaderBoardItemPattern> leaderboard;

    public List<LeaderBoardItemPattern> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<LeaderBoardItemPattern> leaderboard) {
        this.leaderboard = leaderboard;
    }
}
