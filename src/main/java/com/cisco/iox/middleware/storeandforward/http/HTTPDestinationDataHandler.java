package com.cisco.iox.middleware.storeandforward.http;

import com.cisco.iox.mlib.messaging.ServiceException;
import com.cisco.middleware.storeandforward.MessageFrame;
import com.cisco.middleware.storeandforward.StoreAndForwardErrorCode;
import com.cisco.middleware.storeandforward.destination.DestinationException;
import com.cisco.middleware.storeandforward.destination.DestinationUtil;
import com.cisco.middleware.storeandforward.destination.IOutputMessageQ;
import com.cisco.middleware.storeandforward.destination.OutputQException;
import com.cisco.middleware.storeandforward.destination.common.AbstractDestinationDataHandler;
import com.cisco.middleware.storeandforward.destination.common.EDestinationState;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class HTTPDestinationDataHandler extends AbstractDestinationDataHandler {
	private static final Logger log = LoggerFactory.getLogger(HTTPDestinationDataHandler.class);

	private String url;
	private long retryTime = 1000;

	// @todo http client instance
	private HTTPClient httpClient;

	public HTTPDestinationDataHandler(String type, String policyName, IOutputMessageQ queue, String url,
			int keepAliveSeconds) {
		super(HTTPDestination.TYPE, policyName, queue, url);
		this.url = url;
		// @todo create http client
		httpClient = new HTTPClient(url, keepAliveSeconds);
	}

	/*
	 * Stops the message process thread created by the start() method. After
	 * stopping processing, cleans up the connection created to the broker.
	 *
	 * @throws MqttException
	 *
	 * @throws ConnectorException
	 */
	public void stop() throws DestinationException {
		super.stop();
		try {
			// @todo disconnect http client
			httpClient.disconnect();
		} catch (Exception e) {
			log.error("Disconnect error for DestinationHandler", e);
			throw new HTTPDestinationException(e.getMessage());
		}
	}

	/*
	 * Method to perform HTTP message publishing.
	 *
	 * 1. Creates the HTTP Message 2. Checks the connection to the destination
	 * service, re-tries if needed. 3. Publishes messages to the destination
	 * service. 4. Updates success/failure stats
	 */
	@Override
    public void onData(MessageFrame[] messageFrame) throws InterruptedException {
        String payload;
        	for (MessageFrame eachMessageFrame : messageFrame) {
        		try {
					payload = DestinationUtil.getInstance().getSenMLTuple(eachMessageFrame);
				} catch (JsonProcessingException e) {
	                log.error("Failed to convert to SenML tuple {}", eachMessageFrame.getMsgId() , e);
	                throw new ServiceException(StoreAndForwardErrorCode.INTERNAL_SERVICE_ERROR, "Failure in converting to SenML tuples", e);
	            } catch (DestinationException e) {
	            	log.error("Failed to publish {}", eachMessageFrame.getMsgId());
	                throw new ServiceException(StoreAndForwardErrorCode.INTERNAL_SERVICE_ERROR, "Failure in converting to SenML tuples", e);
				}
                //@todo create HTTPRequest from message
                HTTPRequest request = new HTTPRequest(payload);

                while (true) {
                    // Connect to Broker
                    try {
                        reconnect();
                        break;
                    } catch (Exception e) {
                        this.waitAWhile(this.retryTime);
                        continue;
                    }
                }
                // Publish the message
                try {
                    // @todo execute http request
                    httpClient.execute(request);
                    onDeliveryComplete(eachMessageFrame);
                } catch (Exception e) {
                    log.error("Sensor value publish failed for DestinationHandler");
                    updateMessageFailureStats(e);
                    onDeliveryFail(eachMessageFrame, e);
                }
        	}
     }

	private void onDeliveryComplete(MessageFrame messageFrame) {
		log.debug("The delivery completed for msgId : {}", messageFrame.getMsgId());
		Long msgIds[] = new Long[] { messageFrame.getMsgId() };

		try {
			this.queue.ackMessageIds(msgIds);
			this.statsObj.incrMsgsSent();
			int numberOfBytesSent = 10; // @todo compute number of bytes sent
			this.statsObj.addBytes(numberOfBytesSent);
		} catch (OutputQException e) {
			log.error("DestinationHandler queue ack failure {}", e);
		} catch (Exception e) {
			log.error("DestinationHandler htt ack failure {}", e);
		}
	}

	private void onDeliveryFail(MessageFrame messageFrame, Throwable error) {
		this.statsObj.incrMsgFailures();
		this.statsObj.setLastFailure(error.getMessage());
		Long msgId = (Long) messageFrame.getMsgId();
		try {
			this.getQueue().requeueMessage(msgId);
		} catch (OutputQException e) {
			log.error("Message requeue failed : {}", e);
			return;
		}
		log.info("The message {} successfully requeued", msgId);
	}

	private void updateConnectionFailureStats() throws InterruptedException {
		if (this.connState == EDestinationState.CONNECTED) {
			this.statsObj.incrConnectionFailures();
			this.statsObj.setLastDownTime(Calendar.getInstance().getTimeInMillis());
			this.checkAndChangeState(EDestinationState.DISCONNECTING);
		}
	}

	private void updateMessageFailureStats(Throwable error) {
		if (this.connState == EDestinationState.CONNECTED) {
			this.statsObj.setLastFailure(error.getMessage());
			this.statsObj.incrMsgFailures();
		}
	}

	private void disconnect() throws InterruptedException {
		if (this.connState == EDestinationState.DISCONNECTING) {
			log.error("Disconnecting : {}", this.url);
			// disConnect to Broker
			try {
				// @todo disconnect from server
				httpClient.disconnect();

			} catch (Exception e) {
				log.error("Disconnect Failed for DestinationHandler {} : {}", url, e);
			}
			this.checkAndChangeState(EDestinationState.DISCONNECTED);
		}
	}

	private void createNewConnection() throws InterruptedException {
		if (this.verifyAndChangeState(EDestinationState.DISCONNECTED, EDestinationState.CONNECTING)
				|| this.verifyAndChangeState(EDestinationState.INIT, EDestinationState.CONNECTING)) {
			log.info("Established new Connection : {}", this.url);
		}
	}

	private void try_connect() throws Exception {
		httpClient.connect();
		if (httpClient.isConnected()) {
			this.verifyAndChangeState(EDestinationState.CONNECTING, EDestinationState.CONNECTED);
			log.info("DestinationHandler {} successfully connected to the service ");
			this.queue.requeuePendingMessages();

		} else {
			this.checkAndChangeState(EDestinationState.CONNECTING);
			httpClient.disconnect();
			throw new Exception("not connected");
		}
	}

	private void reconnect() throws Exception {
		if (httpClient.isConnected()) {
			return;
		} else {
			log.error("DestinationHandler not connected, cleaning up : {}");
			// Updates if the connection is in connected state
			updateConnectionFailureStats();
			// Disconnects if the connection is in disconnecting state
			disconnect();
		}

		// if connection disconnected, then create new connection.
		createNewConnection();

		// Connect to Broker
		try {
			try_connect();
		} catch (Exception e) {
			log.error("Failed DestinationHandler : {} with error", e);
			this.checkAndChangeState(EDestinationState.CONNECTING);
			throw e;
		}
	}


	@Override
	public int getBatchSize() {
		return 1;
	}
}
