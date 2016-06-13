package com.cisco.iox.middleware.storeandforward.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPClient {
    private String url;
    private int keepAliveSeconds;
    private boolean connected;

    public HTTPClient(String url, int keepAliveSeconds) {
        this.url = url;
        this.keepAliveSeconds = keepAliveSeconds;
    }

    public boolean isConnected(){
        return connected;
    }

    public void connect(){
        // @todo implement connect
        connected = true;
    }

    public void disconnect() {
        // @todo implement disconnect
        connected = false;
    }

    public void execute(HTTPRequest request) throws Exception{
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/senml+xml");
        con.setDoOutput(true);
        OutputStream out = con.getOutputStream();
        byte bytes[] = request.getPayload().getBytes();
        out.write(bytes);
        out.close();

        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        con.disconnect();
    }
}
