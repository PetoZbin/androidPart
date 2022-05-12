package com.example.orienteering.competition;

import androidx.annotation.NonNull;

public enum CompetitorStates {

    JOINED("JOINED"),
    ONBOARDING("ONBOARDING"),   //stav kedy caka na potvrdenie servera a start preteku
    PERFORMING("PERFORMING"),
    QUIT("QUIT"),
    SPECTATOR("SPECTATOR"),
    FINISHED("FINISHED");


    private final String text;

    CompetitorStates(final String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
