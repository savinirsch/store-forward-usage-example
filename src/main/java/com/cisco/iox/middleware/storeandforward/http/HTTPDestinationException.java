package com.cisco.iox.middleware.storeandforward.http;

import com.cisco.middleware.storeandforward.destination.DestinationException;

public class HTTPDestinationException  extends DestinationException {
    private static final long serialVersionUID = 1L;

    public HTTPDestinationException(String message) {
        super(message);
    }
}

