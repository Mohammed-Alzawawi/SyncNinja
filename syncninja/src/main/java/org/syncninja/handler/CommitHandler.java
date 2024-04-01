package org.syncninja.handler;

import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.CommitController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;


public class CommitHandler extends BaseHandler{
    private final CommitController commitController;

    public CommitHandler() {
        this.commitController = new CommitController();
    }

    @Override
    protected String handleRequest(JSONObject jsonObject) throws Exception {
        String path = jsonObject.getString("path");
        String message = jsonObject.getString("message");

        commitController.run(path, message);
        return ResourceMessagingService.getMessage(ResourceBundleEnum.COMMIT_SUCCESSFULLY);
    }
}