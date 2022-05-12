package com.example.orienteering.helpers;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    public static final String SK_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static String convertIsoToSk(String isoDateStr){

        try{

            Date date = new SimpleDateFormat(ISO_DATE_FORMAT, Locale.getDefault()).parse(isoDateStr);

            return new SimpleDateFormat(SK_DATE_FORMAT, Locale.getDefault()).format(date);

        }catch (ParseException |NullPointerException ex){

            Log.e("Date helper - convertIsoToSk: " ,"Date converting parse ex");
            return "-";
        }
    }

}
