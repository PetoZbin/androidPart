package com.example.orienteering.competition;

import androidx.annotation.NonNull;

public enum CompetitionStates {

    AWAITING("AWAITING"),
    ONGOING("ONGOING"),
    FINALIZED("FINALIZED"),
    CANCELED("CANCELED");

    private final String text;


    CompetitionStates(final String text) {
        this.text = text;
    }


    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
