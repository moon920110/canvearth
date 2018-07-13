package com.canvearth.canvearth.server;

import java.util.Date;

public class RegisteredSketch {
    public String registeredUserToken;
    public String registeredTime;

    public RegisteredSketch(){
        // default constructor needed for firebase
    }

    public RegisteredSketch(String userToken, Date time) {
        registeredUserToken = userToken;
        registeredTime = time.toString();
    }
}
