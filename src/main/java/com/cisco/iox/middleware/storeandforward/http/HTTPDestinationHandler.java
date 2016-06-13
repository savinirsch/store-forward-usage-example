package com.cisco.iox.middleware.storeandforward.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.middleware.storeandforward.Destination;
import com.cisco.middleware.storeandforward.DestinationDeserializer;
import com.cisco.middleware.storeandforward.destination.IOutputMessageQ;
import com.cisco.middleware.storeandforward.destination.common.DestinationTunables;
import com.cisco.middleware.storeandforward.destination.common.IDestinationDataHandler;
import com.cisco.middleware.storeandforward.destination.common.StoreAndForwardDestinationHandler;

public class HTTPDestinationHandler extends StoreAndForwardDestinationHandler {
    private static final Logger logger = LoggerFactory.getLogger(HTTPDestinationHandler.class);

    @Override
    public void init() {
        logger.info("In {} Init", getType());
        readTunables();
    }

    public DestinationTunables readTunables() {
        final String KEEP_ALIVE = "keepAliveSeconds";
        HTTPTunables tunables = HTTPTunables.getInstance();
        Map<String, Object> moduleConfig = this.getModuleDetails().getModuleConfiguration();

        String keepAlive = (moduleConfig.containsKey(KEEP_ALIVE)) ? moduleConfig.get(KEEP_ALIVE).toString() : null;
        if (keepAlive != null) tunables.setKeepAlive(Integer.parseInt(keepAlive));

        // @todo read tunable parameters from moduleConfig

        return tunables;
    }

    @Override
    public String getType() {
        return HTTPDestination.TYPE;
    }

    @Override
    public DestinationDeserializer getDestinationDeserializer() {
        return new HTTPDestinationDeserializer();
    }

    /**
     * Method helps validating the properties sent for the HTTP destination. Used
     * during the provisioning of the policy, via REST handler.
     *
     * @param destination
     * @throws HTTPDestinationException
     */
    @Override
    public void validateConfig(Destination destination) throws HTTPDestinationException {
        HTTPDestination dps = (HTTPDestination) destination;
        if (!dps.getHttpProperties().containsKey(Destination.HOST_PROP)) {
            throw new HTTPDestinationException("No host in the config");
        }

        if (!dps.getHttpProperties().containsKey(Destination.PORT_PROP)) {
            throw new HTTPDestinationException("No Port in the config");
        }

        // @todo validate http destination properties
    }

    /**
     * This method creates the DestinationDataHandler implementation object from the
     * properties sent during the policy creation by the REST handler.
     *
     * @param policyName
     * @param destination
     * @param queue
     * @return
     * @throws NumberFormatException
     * @throws HTTPDestinationException
     */
    @Override
    public IDestinationDataHandler createDestinationDataHandler(String policyName, Destination destination, IOutputMessageQ queue) throws HTTPDestinationException {
        HTTPDestination httpDestination = (HTTPDestination) destination;

        if (policyName == null || policyName.length() == 0) {
            throw new HTTPDestinationException("No parameters in the policy");
        }

        if (httpDestination == null || (httpDestination.getHttpProperties() == null)) {
            throw new HTTPDestinationException("No parameters in the policy");
        }

        if (queue == null) {
            throw new HTTPDestinationException("No destination queue in the policy");
        }

        validateConfig(httpDestination);

        Map<String, Object> httpProperties = httpDestination.getHttpProperties();

        HTTPTunables tunables = HTTPTunables.getInstance();
        String host = httpProperties.get(Destination.HOST_PROP).toString();
        String port = httpProperties.get(Destination.PORT_PROP).toString();

        String url = "http://" + host + ":" + port;

        String keepAlive = (httpProperties
                .containsKey(HTTPDestination.KEEPALIVE_PROP) ? httpProperties
                .get(HTTPDestination.KEEPALIVE_PROP).toString()
                : null);

        if (keepAlive == null || keepAlive.isEmpty()) {
            keepAlive = "" + tunables.getKeepAlive();
        }

        HTTPDestinationDataHandler dataHandler;
        try {
            dataHandler = new HTTPDestinationDataHandler(getType(), policyName, queue, url, Integer.parseInt(keepAlive.trim()));
        } catch (Exception e) {
            throw new HTTPDestinationException("HTTPDestinationDataHandler creation failed: "+ e);
        }

        return dataHandler;
    }

	
}
