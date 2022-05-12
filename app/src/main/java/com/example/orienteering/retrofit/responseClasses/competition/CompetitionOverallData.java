package com.example.orienteering.retrofit.responseClasses.competition;

import android.util.Log;

import com.example.orienteering.competition.run.dialogues.finishRecycler.FinishRecord;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.retrofit.patternClasses.CompetitionPattern;
import com.example.orienteering.retrofit.patternClasses.CompetitorPattern;
import com.example.orienteering.retrofit.patternClasses.LeaderBoardItemPattern;
import com.example.orienteering.retrofit.patternClasses.LeaderboardWaypoint;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CompetitionOverallData extends CompetitionPattern {

    public static final String SK_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @SerializedName("competitionId")
    private String competitionId;

    @SerializedName("competitorsList")
    private List<CompetitorPattern> competitorsList;


    public List<CompetitorPattern> getCompetitorsList() {
        return competitorsList;
    }

    public void setCompetitorsList(List<CompetitorPattern> competitorsList) {
        this.competitorsList = competitorsList;
    }

    public String getCompetitionId() {
        return this.competitionId;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }



    public Boolean isCompetitor(String userId){

        for (CompetitorPattern competitor : competitorsList){

            if (competitor.getCompetitorId().equals(userId)){
                return true;
            }
        }
        return false;
    }

    public Boolean isFreePlace(){

        if (competitorsList.size() < getMaxCompetitors()){ //da sa prihlasit
            return true;
        }
        return false;
    }

    public Boolean notStartedYet() throws ParseException, NullPointerException {

        //aktualny cas je mensi ako timelimit - 1 minuta
        //prihlasenie je nutne 60+ sekund pred startom

        Date now = new Date();

        Date runDatetime = new SimpleDateFormat(ISO_DATE_FORMAT, Locale.getDefault()).parse(getCompDateTime());
        runDatetime.setTime(runDatetime.getTime() - (60*1000));

        if(runDatetime.compareTo(now) > 0){ // sutaz-1min je neskorsie ako terajsi datum, je cas sa prihlasit .. prihlasit sa je mozne najneskorsie minutu pred startom

            return true;
        }

        return false;
    }

    public Boolean isUserOwner(String userId){   //ci zadal current user danu sutaz

        if (getOrganizerId().equals(userId)){
            return true;
        }
        return false;
    }

    public CompetitorPattern getCompetitorById(String competitorId){

        try {

            for (CompetitorPattern competitor : competitorsList){

                if (competitor.getCompetitorId().equals(competitorId)){

                    return competitor;
                }
            }

        }catch (NullPointerException ex){
            Log.e("getCompetitorById ", "Null ptr exception");
        }

        return null;
    }

    //date skoncenia sutaze

    public Date getCompetitionEndDate() throws ParseException{

        //datum sutaze + duration
        Date startDate = new SimpleDateFormat(ISO_DATE_FORMAT, Locale.getDefault()).parse(getCompDateTime());

        return (new Date(startDate.getTime() + getDurationMins()* 60000L) );
    }

    // datum a cas kedy skonci sutaz v Sk formate

// preliminary standing - predbezne poradie sutaziaceho - doterajsie poradie po zadany waypoint (od wp 1 az po waypointNumber )
    public int resolveCompetitorStanding(int waypointNumber, String competitorId) {

        try{

            if (waypointNumber == 1){   //pre prvy waypoint vrat poziciu 0 - este len zacal na starte

                return 0;
            }


            // prejde cez  zaznamy leaderboardu - recent a first waypoint - urci poradie sutaziaceho

            CustomWaypointDesc recentWaypoint = getCheckpointBySeqNum(waypointNumber);
            CustomWaypointDesc firstWaypoint = getCheckpointBySeqNum(1);


            Map<String,Long> leaderMap = new HashMap<>();

            List<CompetitorPattern> recentWPCompetitors = new ArrayList<>();

            // naplnim mapu z posledneho checkpointu - UserId -> cas Long millis

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());

            for (LeaderBoardItemPattern leadItem : recentWaypoint.getLeaderboard()){

                Date date = sdf.parse(leadItem.getArrivalTime());

                if (date == null){ throw new NullPointerException();}

                leaderMap.put(leadItem.getCompetitorId(), date.getTime());
            }

            //od kazdeho casu s danym klucom odratam cas zaciatku sutaze (koncovy cas - pociatocny cas = dosiahnuty vysledny cas)

            for (LeaderBoardItemPattern leadItem : firstWaypoint.getLeaderboard()){

                if (leaderMap.containsKey(leadItem.getCompetitorId())){
                    // dany ucastnik uz dosiahol nas sledovany waypoint

                    Date date = sdf.parse(leadItem.getArrivalTime());

                    Long recentWPTime = leaderMap.get(leadItem.getCompetitorId());

                    Long resultTime = Long.valueOf(recentWPTime.longValue() - date.getTime());

                    leaderMap.put(leadItem.getCompetitorId(),resultTime);
                }
            }

            List<Map.Entry<String, Long>> resultList = new ArrayList<>(leaderMap.entrySet());
            resultList.sort(Map.Entry.comparingByValue());      // casy zoradene


            //najdi sutaziaceho, ktoreho chcem a vrat jeho poradie vramci zoradeneho zoznamu mapovych Entry
            for (int i =0; i< resultList.size(); i++){

                if (resultList.get(i).getKey().equals(competitorId)){
                    return i+1; //vrat poradie
                }
            }
            return 0;   // 0 ak neuspech

        }catch (NullPointerException ex){
            Log.e("competition resolve standing: ","competition data null ptr ex");
            return 0;
        }
        catch (ParseException ex){
            Log.e("competition resolve standing:","date parse exception");
            return 0;
        }
    }

    //cas medzi konkretnymi waypointami
    public String durationTimeBetweenWaypoints(int seqNumBegin, int seqNumEnd, String competitorId){

        try{

            CustomWaypointDesc beginWp = getCheckpointBySeqNum(seqNumBegin);
            CustomWaypointDesc endWp = getCheckpointBySeqNum(seqNumEnd);

            // ziskaj cas uzivatela v pociatocnom a konecnom waypointe
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());

            LeaderBoardItemPattern recordBegin = beginWp.getLeaderBoardRecordByCompetitorId(competitorId);
            LeaderBoardItemPattern recordEnd = endWp.getLeaderBoardRecordByCompetitorId(competitorId);

            //odcitaj casy

            Long beginTime = sdf.parse(recordBegin.getArrivalTime()).getTime();
            Long endTime = sdf.parse(recordEnd.getArrivalTime()).getTime();

            long resultTime = endTime - beginTime;

            return new SimpleDateFormat("HH:mm:ss.SS",Locale.getDefault()).format(new Date(resultTime));

        }catch (NullPointerException ex){

            Log.e("durationTimeBetweenWaypoints: ","competition data null ptr ex");
            return "";
        }catch (ParseException ex){
            Log.e("durationTimeBetweenWaypoints: ","date parse exception");
            return "";
        }
    }

    public CompetitorPattern getWinner(){

        try{
            for (CompetitorPattern competitor : competitorsList){

                if (competitor.getTotalRank().trim().equals("1")){
                    return competitor;
                }
            }

        }catch (NullPointerException ex){
            Log.e("competitorPattern - getWinner: ","data null ptr ex");
            return null;
        }

        return null;
    }

    public int getCompetitorsNum(){

        if (competitorsList != null){
            return competitorsList.size();
        }
        return 0;
    }


    //funkcia pripravi zannam o kazdom waypointe pre uzivatela
    public List<FinishRecord> prepareRecords(String userId){

        if ((userId == null)){
            return new ArrayList<>();
        }

        //pripravy polozky recycler view - thoroughfare + waypoint sequence + cas od zaciatku po dany wp

        List<FinishRecord> resultList = new ArrayList<>();

        for (CustomWaypointDesc waypoint : getWaypointList()){

            LeaderBoardItemPattern record = waypoint.getLeaderBoardRecordByCompetitorId(userId);

            if (record == null){
                continue;   // mali by byt zoradene podla sekvencneho cisla  - pre istotu cez vsetky
            }

            FinishRecord fr = new FinishRecord();
            fr.setThoroughfare(waypoint.getThoroughfare());
            fr.setSeqNum(waypoint.getSeqNumber());
            fr.setCheckpointTime(durationTimeBetweenWaypoints(1,waypoint.getSeqNumber(),userId));
            resultList.add(fr);
        }

        return resultList;
    }
}
