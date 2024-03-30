package org.syncninja.handler;

import com.sun.net.httpserver.HttpExchange;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.CommitController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;


public class CommitHandler extends BaseHandler{
    CommitController commitController;

    public CommitHandler() {
        this.commitController = new CommitController();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("POST".equals(exchange.getRequestMethod())) {
                JSONObject jsonObject = getRequest(exchange);

                String path = jsonObject.getString("path");
                String message = jsonObject.getString("message");

                commitController.run(message, path);
                String response = ResourceMessagingService.getMessage(ResourceBundleEnum.COMMIT_SUCCESSFULLY);

                sendResponse(exchange, response);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
