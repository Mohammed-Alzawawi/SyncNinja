package org.syncninja.handler;

import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.StatusController;

public class StatusHandler extends BaseHandler{
    private final StatusController statusController;

    public StatusHandler() {
        this.statusController = new StatusController();
    }

    @Override
    protected String handleRequest(JSONObject jsonObject) throws Exception {
        String path = jsonObject.getString("path");
        return statusController.run(path);
    }
}
