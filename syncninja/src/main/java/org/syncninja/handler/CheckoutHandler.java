package org.syncninja.handler;

import com.sun.net.httpserver.HttpExchange;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.controller.CheckoutController;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;

public class CheckoutHandler extends BaseHandler{
    CheckoutController checkoutController;

    public CheckoutHandler() {
        this.checkoutController = new CheckoutController();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("POST".equals(exchange.getRequestMethod())) {
                JSONObject jsonObject = getRequest(exchange);

                String path = jsonObject.getString("path");
                String branchName = jsonObject.getString("name");
                boolean isNewBranch = jsonObject.getBoolean("isNewBranch");

                checkoutController.run(path, branchName, isNewBranch);

                String response = ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_CHECKED_OUT_SUCCESSFULLY, new Object[]{branchName});
                if (isNewBranch){
                    response = ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_ADDED_SUCCESSFULLY, new Object[]{branchName});
                }

                sendResponse(exchange, response);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
