package org.syncninja.handler;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.RestoreController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;

import java.util.List;

public class RestoreHandler extends BaseHandler{
    private final RestoreController restoreController;

    public RestoreHandler() {
        this.restoreController = new RestoreController();
    }

    @Override
    protected String handleRequest(JSONObject jsonObject) throws Exception {
        String path = jsonObject.getString("path");
        JSONArray restoreFilesJSON = jsonObject.getJSONArray("files");
        List<String> restoreFiles = jsonToList(restoreFilesJSON);

        restoreController.run(path, restoreFiles);
        return ResourceMessagingService.getMessage(ResourceBundleEnum.RESTORED_SUCCESSFULLY);
    }
}