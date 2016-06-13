package com.cisco.iox.middleware.storeandforward.http;

import com.cisco.iox.middleware.module.ModuleException;
import com.cisco.iox.mlib.messaging.Service;
import com.cisco.middleware.storeandforward.StoreAndForwardService;
import com.cisco.middleware.storeandforward.destination.DestinationRegistry;

@Service(HTTPStoreAndForwardService.SERVICE_NAME)
public class HTTPStoreAndForwardService extends StoreAndForwardService {
	
    public HTTPStoreAndForwardService(String serviceName) {
		super(serviceName);
	}

	static final String SERVICE_NAME = "custom:service:httpstoreandforward";

    protected  String getServiceName(){
        return SERVICE_NAME;
    }

    protected void registerDestinations(DestinationRegistry registry) throws ModuleException {
        registry.register(new HTTPDestinationHandler());
    }

}

