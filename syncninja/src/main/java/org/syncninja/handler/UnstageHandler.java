package org.syncninja.handler;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.UnstageController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;

import java.util.List;

public class UnstageHandler extends BaseHandler{
    private final UnstageController unstageController;

    public UnstageHandler() {
        this.unstageController = new UnstageController();
    }

    @Override
    protected String handleRequest(JSONObject jsonObject) throws Exception {
        String path = jsonObject.getString("path");
        JSONArray unstageFileJSON = jsonObject.getJSONArray("files");
        List<String> unstageFiles = jsonToList(unstageFileJSON);

        unstageController.run(path, unstageFiles);
        return ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_REMOVED);
    }
}