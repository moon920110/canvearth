package com.canvearth.canvearth.server;

import java.util.Date;

public class RegisteredSketch {
    public String registeredUserToken;
    public String registeredTime;
    public String sketchName;

    public RegisteredSketch(){
        // default constructor needed for firebase
    }

    public RegisteredSketch(String userToken, Date time, String sketchName) {
        registeredUserToken = userToken;
        registeredTime = time.toString();
        this.sketchName = sketchName;
    }
}
