package com.example.orienteering.userAccess.onboarding.responses;

public class RegFailResponse {

    private Boolean validationError = false;
    private Boolean serverError = false;
    private Boolean dbFailed = false;
    private Boolean usernameError = false;
    private Boolean emailError = false;
    private Boolean privateKeyError = false;
    private Boolean address = false;
    private Boolean passwordError = false;

    private String message;

    private String userNameMsg;
    private String emailMsg;
    private String passwordMsg;
    private String privateKeyMsg;
    private String addressMsg;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserNameMsg() {
        return userNameMsg;
    }

    public void setUserNameMsg(String userNameMsg) {
        this.userNameMsg = userNameMsg;
    }

    public String getEmailMsg() {
        return emailMsg;
    }

    public void setEmailMsg(String emailMsg) {
        this.emailMsg = emailMsg;
    }

    public String getPasswordMsg() {
        return passwordMsg;
    }

    public void setPasswordMsg(String passwordMsg) {
        this.passwordMsg = passwordMsg;
    }

    public String getPrivateKeyMsg() {
        return privateKeyMsg;
    }

    public void setPrivateKeyMsg(String privateKeyMsg) {
        this.privateKeyMsg = privateKeyMsg;
    }

    public String getAddressMsg() {
        return addressMsg;
    }

    public void setAddressMsg(String addressMsg) {
        this.addressMsg = addressMsg;
    }

    public Boolean getDbFailed() {
        return dbFailed;
    }

    public void setDbFailed(Boolean dbFailed) {
        this.dbFailed = dbFailed;
    }

    public Boolean getUsernameError() {
        return usernameError;
    }

    public void setUsernameError(Boolean usernameError) {
        this.usernameError = usernameError;
    }

    public Boolean getEmailError() {
        return emailError;
    }

    public void setEmailError(Boolean emailError) {
        this.emailError = emailError;
    }

    public Boolean getPrivateKeyError() {
        return privateKeyError;
    }

    public void setPrivateKeyError(Boolean privateKeyError) {
        this.privateKeyError = privateKeyError;
    }

    public Boolean getAddress() {
        return address;
    }

    public void setAddress(Boolean address) {
        this.address = address;
    }

    public Boolean getPasswordError() {
        return passwordError;
    }

    public void setPasswordError(Boolean passwordError) {
        this.passwordError = passwordError;
    }

    public Boolean getServerError() {
        return serverError;
    }

    public void setServerError(Boolean serverError) {
        this.serverError = serverError;
    }

    public Boolean getValidationError() {
        return validationError;
    }

    public void setValidationError(Boolean validationError) {
        this.validationError = validationError;
    }
}
