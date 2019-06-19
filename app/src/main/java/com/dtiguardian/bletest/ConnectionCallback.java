package com.dtiguardian.bletest;

import java.util.List;

public abstract class ConnectionCallback {
    public void onFinishConnect(boolean connected) {
        throw new RuntimeException("Stub!");
    }

    public void onReadWrite(String msg){
        throw new RuntimeException("Stub!");
    }
}
