package org.syncninja.handler;

import com.sun.net.httpserver.HttpExchange;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.AddController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;

import java.util.ArrayList;
import java.util.List;

public class AddHandler extends BaseHandler {
    AddController addController;

    public AddHandler() {
        this.addController = new AddController();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("POST".equals(exchange.getRequestMethod())) {
                // read request
                JSONObject jsonObject = getRequest(exchange);

                String path = jsonObject.getString("path");
                JSONArray addFilesJSON = jsonObject.getJSONArray("files");
                List<String> addFiles = jsonToList(addFilesJSON);

                // get response
                addController.run(path, addFiles);
                String response = ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_ADDED);

                // Send response to the client
                sendResponse(exchange, response);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
