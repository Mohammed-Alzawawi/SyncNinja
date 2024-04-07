package org.syncninja.handler;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.AddController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;

import java.util.List;

public class AddHandler extends BaseHandler {
    private final AddController addController;

    public AddHandler() {
        this.addController = new AddController();
    }

    @Override
    protected String handleRequest(JSONObject jsonObject) throws Exception {
        String path = jsonObject.getString("path");
        JSONArray addFilesJSON = jsonObject.getJSONArray("files");
        List<String> addFiles = jsonToList(addFilesJSON);

        addController.run(path, addFiles);
        return ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_ADDED);
    }
}