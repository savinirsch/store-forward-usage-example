package com.cisco.iox.middleware.storeandforward.http;

import com.cisco.middleware.storeandforward.destination.common.DestinationTunables;

public class HTTPTunables extends DestinationTunables{
    private static final HTTPTunables instance = new HTTPTunables();

    private int keepAlive = 60;

    private HTTPTunables(){}

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public static HTTPTunables getInstance(){
        return instance;
    }
}
