package com.canvearth.canvearth.server;

import com.canvearth.canvearth.pixel.FBPixelDataSquare;

import java.util.Date;

public class RegisteredSketch {
    public String registeredUserToken;
    public String registeredTime;
    public String sketchName;
    public FBPixelDataSquare fbPixelDataSquare;

    public RegisteredSketch(){
        // default constructor needed for firebase
    }

    public RegisteredSketch(String userToken, Date time, String sketchName, FBPixelDataSquare fbPixelDataSquare) {
        registeredUserToken = userToken;
        registeredTime = time.toString();
        this.sketchName = sketchName;
        this.fbPixelDataSquare = fbPixelDataSquare;
    }
}
