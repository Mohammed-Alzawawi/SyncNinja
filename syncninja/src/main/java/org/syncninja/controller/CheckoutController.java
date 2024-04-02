package org.syncninja.controller;

import org.syncninja.service.CheckoutService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;

public class CheckoutController {
    private final CheckoutService checkoutService;
    private final StateTreeService stateTreeService;

    public CheckoutController() {
        this.checkoutService = new CheckoutService();
        this.stateTreeService = new StateTreeService();
    }

    public void run(String path, String branchName, boolean isNewBranch) throws Exception {
        if (isNewBranch) {
            checkoutService.createNewBranch(branchName, path);
        } else if(stateTreeService.getStateRoot(path).getCurrentBranch().getName().equals(branchName)){
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.YOU_ARE_ALREADY_IN_BRANCH, new Object[]{branchName}));
        } else {
            checkoutService.checkout(branchName, path);
        }
        Neo4jSession.closeSession();
    }
}
