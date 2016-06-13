package com.cisco.iox.middleware.storeandforward.http;

import com.cisco.middleware.storeandforward.DestinationDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class HTTPDestinationDeserializer  implements DestinationDeserializer<HTTPDestination> {
    @Override
    public HTTPDestination deserialize(JsonNode node) {
        HTTPDestination httpDestination = new HTTPDestination();
        JsonNode propNode = node.get("httpProperties");
        httpDestination.setHttpProperties(new ObjectMapper().convertValue(propNode, Map.class));
        return httpDestination;
    }
}

