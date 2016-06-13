package com.cisco.iox.middleware.storeandforward.http;

public class HTTPRequest {
    private String payload;

    public HTTPRequest(String payload){
        this.payload = payload;
    }

    public String getPayload(){
        return payload;
    }
}
