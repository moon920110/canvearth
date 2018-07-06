package com.canvearth.canvearth.utils.concurrency;

public class Success {
    private boolean value = false;

    public boolean isSuccessed() {
        return value;
    }

    public void setSuccess() {
        value = true;
    }

    public void setFail() {
        value = false;
    }
}
