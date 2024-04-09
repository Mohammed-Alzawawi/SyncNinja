package org.syncninja.command;

import org.neo4j.ogm.session.Session;
import org.syncninja.service.CheckoutService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

@CommandLine.Command(name = "checkout")
public class CheckoutCommand extends BaseCommand {

    @CommandLine.Parameters(paramLabel = "branch name", description = "name your branch")
    private String branchName;

    @CommandLine.Option(names = {"-b"}, paramLabel = "new branch name")
    private boolean isNewBranch;

    private final CheckoutService checkoutService;
    private final StateTreeService stateTreeService;

    public CheckoutCommand() {
        this.checkoutService = new CheckoutService();
        this.stateTreeService = new StateTreeService();
    }

    @Override
    public void run() {
        try {
            Session session = Neo4jSession.getSession();
            session.beginTransaction();

            String path = System.getProperty("user.dir");
            if (isNewBranch) {
                checkoutService.createNewBranch(branchName, path);
                System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_ADDED_SUCCESSFULLY, new Object[]{branchName}));
            } else if(stateTreeService.getStateRoot(path).getCurrentBranch().getName().equals(branchName)){
                throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.YOU_ARE_ALREADY_IN_BRANCH, new Object[]{branchName}));
            } else {
                checkoutService.checkout(branchName, path);
                System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_CHECKED_OUT_SUCCESSFULLY, new Object[]{branchName}));
            }

            session.getTransaction().commit();
            Neo4jSession.closeSession();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}