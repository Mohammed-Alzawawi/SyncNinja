package org.syncninja.controller;

import org.syncninja.service.CheckoutService;
import org.syncninja.util.Neo4jSession;

public class CheckoutController {
    private final CheckoutService checkoutService;

    public CheckoutController() {
        this.checkoutService = new CheckoutService();
    }

    public void run(String path, String branchName, boolean isNewBranch) throws Exception {
        if (isNewBranch) {
            checkoutService.createNewBranch(branchName, path);
        } else {
            checkoutService.checkout(branchName, path);
        }
        Neo4jSession.closeSession();
    }
}
