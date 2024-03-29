package org.syncninja.controller;

import org.syncninja.util.OutputCollector;
import org.syncninja.service.CheckoutService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;

public class CheckoutController {
    private final CheckoutService checkoutService;

    public CheckoutController() {
        this.checkoutService = new CheckoutService();
    }

    public void run(String path, String branchName, boolean isNewBranch) {
        try {
            if (isNewBranch) {
                checkoutService.createNewBranch(branchName, path);
                OutputCollector.addString(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_ADDED_SUCCESSFULLY, new Object[]{branchName}));
            } else {
                checkoutService.checkout(branchName, path);
                OutputCollector.addString(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_CHECKED_OUT_SUCCESSFULLY, new Object[]{branchName}));
            }
            Neo4jSession.closeSession();
        } catch (Exception exception) {
            OutputCollector.addString(exception.getMessage());
        }
    }
}
