package org.syncninja.handler;

import com.sun.net.httpserver.HttpExchange;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.UnstageController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;

import java.util.List;

public class UnstageHandler extends BaseHandler{
    UnstageController unstageController;

    public UnstageHandler() {
        this.unstageController = new UnstageController();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("POST".equals(exchange.getRequestMethod())) {
                // read request
                JSONObject jsonObject = getRequest(exchange);

                String path = jsonObject.getString("path");
                JSONArray unstageFileJSON = jsonObject.getJSONArray("files");
                List<String> unstageFiles = jsonToList(unstageFileJSON);

                // get response
                unstageController.run(path, unstageFiles);
                String response = ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_REMOVED);

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
