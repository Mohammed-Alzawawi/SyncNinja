package org.syncninja.handler;

import com.sun.net.httpserver.HttpExchange;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.RestoreController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;

import java.io.IOException;
import java.util.List;

public class RestoreHandler extends BaseHandler{
    RestoreController restoreController;

    public RestoreHandler() {
        this.restoreController = new RestoreController();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("POST".equals(exchange.getRequestMethod())) {
                JSONObject jsonObject = getRequest(exchange);

                String path = jsonObject.getString("path");
                JSONArray restoreFilesJSON = jsonObject.getJSONArray("files");
                List<String> restoreFiles = jsonToList(restoreFilesJSON);

                restoreController.run(path, restoreFiles);
                String response = ResourceMessagingService.getMessage(ResourceBundleEnum.RESTORED_SUCCESSFULLY);

                sendResponse(exchange, response);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
