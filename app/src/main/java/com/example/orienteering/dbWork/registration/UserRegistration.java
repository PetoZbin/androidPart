package com.example.orienteering.dbWork.registration;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table")
public class UserRegistration {

    @PrimaryKey
    @NonNull
    public String userId;      //vygeneruje server

    @ColumnInfo(name = "logged")    //prave prihlaseny user
    public Boolean logged = false;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "password")      //hash - uvidim, ci potrebujem ukladat
    public String password;

    @ColumnInfo(name = "encryptionKey")      //hashovany
    public String encryptionKey;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "token")
    public String token;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getLogged() {
        return logged;
    }

    public void setLogged(Boolean logged) {
        this.logged = logged;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
}
