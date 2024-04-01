package org.syncninja.command;

import org.syncninja.model.statetree.StateRoot;
import org.syncninja.service.CheckoutService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

@CommandLine.Command(name = "checkout", description = "add a new branch")
public class CheckOutCommand implements Runnable {
    private CheckoutService checkoutService;
    private StateTreeService stateTreeService;

    @CommandLine.Parameters(paramLabel = "branch name", description = "name your branch")
    private String branchName;

    @CommandLine.Option(names = {"-b"}, paramLabel = "new branch name")
    private boolean isNewBranch;

    public CheckOutCommand() {
        this.checkoutService = new CheckoutService();
        this.stateTreeService = new StateTreeService();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        try {
            if (isNewBranch) {
                checkoutService.createNewBranch(branchName, path);
                System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_ADDED_SUCCESSFULLY, new Object[]{branchName}));
            } else if(stateTreeService.getStateRoot(path).getCurrentBranch().getName().equals(branchName)){
                System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.YOU_ARE_ALREADY_IN_BRANCH, new Object[]{branchName}));
            }
            else{
                checkoutService.checkout(branchName, path);
                System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_CHECKED_OUT_SUCCESSFULLY, new Object[]{branchName}));
            }
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}