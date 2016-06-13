package com.cisco.iox.middleware.storeandforward.http;

import com.cisco.middleware.storeandforward.Destination;

import java.util.HashMap;
import java.util.Map;

public class HTTPDestination extends Destination {
    private static final long serialVersionUID = 3573369262670213518L;
    public static final String TYPE = "system:service:storeandforward:destination:http";

    public static final String KEEPALIVE_PROP = "keepAliveSeconds";

    private Map<String, Object> httpProperties = new HashMap<>();

    @Override
    public String getType() {
        return TYPE;
    }

    public Map<String, Object> getHttpProperties() {
        return httpProperties;
    }

    public void setHttpProperties(Map<String, Object> httpProperties) {
        this.httpProperties = httpProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HTTPDestination that = (HTTPDestination) o;

        return !(httpProperties != null ? !httpProperties.equals(that.httpProperties) : that.httpProperties != null);

    }

    @Override
    public int hashCode() {
        return httpProperties != null ? httpProperties.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "HTTPDestination{" +
                "httpProperties=" + httpProperties +
                '}';
    }
}

