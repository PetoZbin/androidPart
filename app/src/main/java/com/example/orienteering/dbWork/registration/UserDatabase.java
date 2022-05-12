package com.example.orienteering.dbWork.registration;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UserRegistration.class, UserCredentials.class, UserCompetition.class} , exportSchema = false, version = 7)
public abstract class UserDatabase extends RoomDatabase {

    public static final String DB_NAME = "users_db";
    private static UserDatabase instance;

    public static synchronized UserDatabase getInstance(Context context){

        if (instance == null){

            instance = Room.databaseBuilder(context.getApplicationContext(),
                    UserDatabase.class, DB_NAME).fallbackToDestructiveMigration().build();
        }
            return instance;
    }

    public abstract UsersDao usersDao();
    public abstract CompetitionDao competitionDao();
}
