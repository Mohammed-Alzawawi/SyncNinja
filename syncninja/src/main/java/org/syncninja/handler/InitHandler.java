package org.syncninja.handler;

import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.InitController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;

public class InitHandler extends BaseHandler {
    private final InitController initController;

    public InitHandler() {
        this.initController = new InitController();
    }

    @Override
    protected String handleRequest(JSONObject jsonObject) throws Exception {
        String path = jsonObject.getString("path");
        initController.run(path);
        return ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_INITIALIZED_SUCCESSFULLY, new Object[]{path});
    }
}