package org.syncninja.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.Main;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Main.restartTimer();
        try {
            if ("POST".equals(exchange.getRequestMethod())) {
                JSONObject jsonObject = getRequest(exchange);
                String response = handleRequest(jsonObject);
                sendResponse(exchange, response);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        } catch (Exception e) {
            sendResponse(exchange, e.getMessage());
        } finally {
            exchange.close();
        }
    }

    protected abstract String handleRequest(JSONObject jsonObject) throws Exception;

    protected JSONObject getRequest(HttpExchange exchange) throws Exception {
        StringBuilder requestBody = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody())) {
            char[] buffer = new char[256];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                requestBody.append(buffer, 0, read);
            }
        }
        return new JSONObject(requestBody.toString());
    }

    protected void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    protected List<String> jsonToList(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>(jsonArray.length());
        for(int i = 0; i < jsonArray.length(); i++){
            list.add(jsonArray.getString(i));
        }
        return list;
    }
}
