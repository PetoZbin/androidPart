package com.example.orienteering.userAccess.accountAdding.responses;


public class AddingSysResponse {

    private Boolean serverError = false;
    private Boolean dbError = false;
    private Boolean pivateKeyInvalid = false;

    private String serverMessage;
    private String dbMessage;
    private String keyMessage;
    private String message;

    public Boolean correctlyAdded(){

        if (serverError || dbError || pivateKeyInvalid){ return false; }    //aspon jedno z nich chyba

        return true;    //ziadna chyba
    }

    public Boolean getServerError() {
        return serverError;
    }

    public void setServerError(Boolean serverError) {
        this.serverError = serverError;
    }

    public Boolean getDbError() {
        return dbError;
    }

    public void setDbError(Boolean dbError) {
        this.dbError = dbError;
    }

    public Boolean getPivateKeyInvalid() {
        return pivateKeyInvalid;
    }

    public void setPivateKeyInvalid(Boolean pivateKeyInvalid) {
        this.pivateKeyInvalid = pivateKeyInvalid;
    }

    public String getServerMessage() {
        return serverMessage;
    }

    public void setServerMessage(String serverMessage) {
        this.serverMessage = serverMessage;
    }

    public String getDbMessage() {
        return dbMessage;
    }

    public void setDbMessage(String dbMessage) {
        this.dbMessage = dbMessage;
    }

    public String getKeyMessage() {
        return keyMessage;
    }

    public void setKeyMessage(String keyMessage) {
        this.keyMessage = keyMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
