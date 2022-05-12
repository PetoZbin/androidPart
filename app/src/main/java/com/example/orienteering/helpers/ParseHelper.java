package com.example.orienteering.helpers;

import android.util.Log;

import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ParseHelper {


    public static String serializeCompetition(CompetitionOverallData competition){

        if ((competition != null)){

            return new Gson().toJson(competition);
        }
        return null;
    }

    public static CompetitionOverallData deserializeCompetition(String competitionJson){

        Type myType = new TypeToken<CompetitionOverallData>(){}.getType();

        return (CompetitionOverallData) new Gson().fromJson(competitionJson,myType);
    }
}
