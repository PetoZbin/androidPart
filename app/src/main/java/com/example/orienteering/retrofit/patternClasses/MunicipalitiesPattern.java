package com.example.orienteering.retrofit.patternClasses;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class MunicipalitiesPattern {

    @SerializedName("municipalities")
    Set<String> municipalities;

    public Set<String> getMunicipalities() {
        return municipalities;
    }

    public void setMunicipalities(Set<String> municipalities) {
        this.municipalities = municipalities;
    }
}
