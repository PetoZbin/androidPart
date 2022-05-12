package com.example.orienteering.dbWork.registration;

import androidx.room.Embedded;

public class UserWithCompetition {

    @Embedded
    private UserRegistration user;


}
