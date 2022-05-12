package com.example.orienteering.userAccess.onboarding.responses;

public class LoginFailResponse {

    private Boolean validationError = false;
    private Boolean serverError = false;
    private Boolean dbFailed = false;
    private Boolean usernameError = false;
    private Boolean passwordError = false;


    private String message;
    private String userNameMsg;
    private String passwordMsg;

    public Boolean getValidationError() {
        return validationError;
    }

    public void setValidationError(Boolean validationError) {
        this.validationError = validationError;
    }

    public Boolean getServerError() {
        return serverError;
    }

    public void setServerError(Boolean serverError) {
        this.serverError = serverError;
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

    public Boolean getPasswordError() {
        return passwordError;
    }

    public void setPasswordError(Boolean passwordError) {
        this.passwordError = passwordError;
    }

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

    public String getPasswordMsg() {
        return passwordMsg;
    }

    public void setPasswordMsg(String passwordMsg) {
        this.passwordMsg = passwordMsg;
    }
}
