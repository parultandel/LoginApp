package com.example.tandels.loginapp.model;

/**
 * Sign In Response model class
 */

public class SignInResponse {

    private int statusCode;

    public SignInResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
