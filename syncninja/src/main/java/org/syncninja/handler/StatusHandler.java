package org.syncninja.handler;

import com.sun.net.httpserver.HttpExchange;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.StatusController;

public class StatusHandler extends BaseHandler{
    StatusController statusController;

    public StatusHandler() {
        this.statusController = new StatusController();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("POST".equals(exchange.getRequestMethod())) {
                JSONObject jsonObject = getRequest(exchange);

                String path = jsonObject.getString("path");
                String response = statusController.run(path);

                sendResponse(exchange, response);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
