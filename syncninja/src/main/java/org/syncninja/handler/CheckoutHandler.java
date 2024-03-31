package org.syncninja.handler;

import com.sun.net.httpserver.HttpExchange;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.CheckoutController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;


public class CheckoutHandler extends BaseHandler{
    private final CheckoutController checkoutController;

    public CheckoutHandler() {
        this.checkoutController = new CheckoutController();
    }

    @Override
    protected String handleRequest(JSONObject jsonObject) throws Exception {
        String path = jsonObject.getString("path");
        String branchName = jsonObject.getString("name");
        boolean isNewBranch = jsonObject.getBoolean("isNewBranch");

        checkoutController.run(path, branchName, isNewBranch);
        String response = ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_CHECKED_OUT_SUCCESSFULLY, new Object[]{branchName});
        if (isNewBranch){
            response = ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_ADDED_SUCCESSFULLY, new Object[]{branchName});
        }
        return response;
    }
}
