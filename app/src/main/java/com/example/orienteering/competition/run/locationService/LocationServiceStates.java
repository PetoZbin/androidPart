package com.example.orienteering.competition.run.locationService;

import androidx.annotation.NonNull;

public enum LocationServiceStates {

    INIT("INIT"),
    RESEND("RESEND"),   //stav kedy caka na potvrdenie servera a start preteku
    LEAVE("LEAVE");


    private final String text;

    LocationServiceStates(final String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
