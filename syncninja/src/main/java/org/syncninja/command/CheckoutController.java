package org.syncninja.command;

import org.syncninja.service.CheckoutService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

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