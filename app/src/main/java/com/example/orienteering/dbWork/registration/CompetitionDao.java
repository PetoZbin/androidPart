package com.example.orienteering.dbWork.registration;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.orienteering.competition.CompetitorStates;

import java.util.List;

@Dao
public interface CompetitionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCompetition(UserCompetition competition);

    @Delete
    void deleteCompetition(UserCompetition competition);
    @Update
    void updateCompetition(UserCompetition competition);

    @Transaction        //update all user's competitions - that are performing or onboarding to Joined (prevent multiple comps in such states)
    @Query("UPDATE user_competition SET competitorStatus = 'JOINED' WHERE userId == (SELECT userId from user_table WHERE logged == '1')" +
            "AND (competitorStatus = 'ONBOARDING' OR competitorStatus = 'PERFORMING')")
    void updateDupliciteActiveCompetitions();

    @Transaction        // deaktivuj vsetky sutaze prihlaseneho uzivatela
    @Query("UPDATE user_competition SET active = '0' WHERE userId == (SELECT userId from user_table WHERE logged == '1')")
    void updateDupliciteActiveCompetitionsToInactive();

    @Transaction
    @Query("SELECT * FROM user_competition WHERE userId == :userId")
    List<UserCompetition> getUserCompetitions(String userId);

    @Transaction
    @Query("SELECT id FROM user_competition WHERE userId == :userId AND competitionId == :competitionId")
    Long getUserCompId(String userId, String competitionId);

    @Transaction
    @Query("SELECT * FROM user_competition WHERE userId == (SELECT userId from user_table WHERE logged == '1') " +
            "AND competitionId == :competitionId")
    UserCompetition getLoggedUserCompetitionById(String competitionId);

    // pre competitor statusy ONBOARDING a PERFORMING - kedze sutaze tychto statusov mozu byt max 1 na uzivatela
    @Transaction
    @Query("SELECT * FROM user_competition WHERE userId == (SELECT userId from user_table WHERE logged == '1') AND competitorStatus == :competitorStatus")
    UserCompetition loggedUserCompetitionByCompetitorStatus(String competitorStatus);


    @Transaction
    @Query("SELECT * FROM user_competition WHERE userId == (SELECT userId from user_table WHERE logged == '1') AND competitorStatus == :competitorStatus")
    LiveData<UserCompetition> loggedLiveUserCompetitionByCompetitorStatus(String competitorStatus);

//takato sutaz je v DB len 1 ↓
    @Transaction
    @Query("SELECT * FROM user_competition WHERE userId == (SELECT userId from user_table WHERE logged == '1')" +
            " AND active == '1' LIMIT 1")
    LiveData<UserCompetition> getLiveLoggedUserActiveCompetition();

    //sutaze do active competition fragmentu - kde je uzivatel owner, joined, performing, quit - pre rychlu volbu
    @Transaction
    @Query("SELECT * FROM user_competition WHERE userId == (SELECT userId from user_table WHERE logged == '1') " +
            "AND (competitorStatus == 'JOINED' OR competitorStatus == 'ONBOARDING' OR competitorStatus == 'PERFORMING'" +
            "OR competitorStatus == 'QUIT' OR isOwner == '1')" +
            "AND (competitionStatus == 'AWAITING' or competitionStatus == 'ONGOING') ")
    List<UserCompetition> getLoggedUsersFocusedCompetitions();

//    @Transaction
//    @Query("SELECT * FROM user_competition WHERE userId == (SELECT userId from user_table WHERE logged == '1')" +
//            " AND (competitorStatus == 'ONBOARDING' OR competitorStatus == 'PERFORMING')")
    @Transaction
    @Query("SELECT * FROM user_competition WHERE userId == (SELECT userId from user_table WHERE logged == '1')" +
            " AND active == '1'")
    List<UserCompetition> getLoggedUserActiveCompetitions();

    @Transaction
    @Query("SELECT EXISTS(SELECT * FROM user_competition WHERE userId == (SELECT userId from user_table WHERE logged == '1') AND competitionId == :competitionId AND competitorStatus == :competitorStatus)")
    Boolean userCompExists(String competitionId, String competitorStatus);

    @Transaction
    @Query("DELETE FROM user_competition WHERE userId == :userId")
    void deleteAllUserCompetitions(String userId);

    @Transaction
    @Query("DELETE FROM user_competition WHERE userId == :userId AND competitionId == :competitionId")
    void deleteUserCompetition(String userId, String competitionId);

    @Transaction
    @Query("SELECT* FROM user_competition WHERE userId == :userId AND" +
            " competitorStatus == :competitorStatus LIMIT 1")
    UserCompetition getUsersPerformingCompetition(String userId, String competitorStatus);

    @Transaction
    @Query("SELECT* FROM user_competition WHERE competitionId == :competitionId")
    UserCompetition getCompetitionById(String competitionId);




}
