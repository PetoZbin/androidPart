package com.example.orienteering.userAccess.onboarding.responses;

public class LoginSuccessResponse extends RegSuccessResponse{

    private String token;
    private Boolean accountPicked = false;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getAccountPicked() {
        return accountPicked;
    }

    public void setAccountPicked(Boolean accountPicked) {
        this.accountPicked = accountPicked;
    }
}
