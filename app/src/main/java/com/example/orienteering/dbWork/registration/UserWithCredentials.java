package com.example.orienteering.dbWork.registration;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class UserWithCredentials {

    @Embedded
    public UserRegistration registration;

    @Relation(parentColumn = "userId", entityColumn = "userId", entity = UserCredentials.class)
    public List<UserCredentials> credentialsList;

    public UserRegistration getRegistration() {
        return registration;
    }

    public void setRegistration(UserRegistration registration) {
        this.registration = registration;
    }

    public List<UserCredentials> getCredentialsList() {
        return credentialsList;
    }

    public void setCredentialsList(List<UserCredentials> credentialsList) {
        this.credentialsList = credentialsList;
    }
}
