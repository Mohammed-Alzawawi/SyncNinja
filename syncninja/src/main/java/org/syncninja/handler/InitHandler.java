package org.syncninja.handler;

import com.sun.net.httpserver.HttpExchange;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.InitController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;

import java.io.*;

public class InitHandler extends BaseHandler {
    InitController initController;

    public InitHandler() {
        this.initController = new InitController();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("POST".equals(exchange.getRequestMethod())) {
                JSONObject jsonObject = getRequest(exchange);

                String path = jsonObject.getString("path");

                initController.run(path);
                String response = ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_INITIALIZED_SUCCESSFULLY, new Object[]{path});

                sendResponse(exchange, response);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}