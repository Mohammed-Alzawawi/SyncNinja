package org.syncninja.command;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.Branch;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.BranchRepository;
import org.syncninja.service.CommitService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

@CommandLine.Command(name = "commit")
public class CommitCommand extends BaseCommand {

    @CommandLine.Option(names = {"-m"}, paramLabel = "message", description = "Enter a message for the commit", required = true)
    private String message;

    private final CommitService commitService;
    private final StateTreeService stateTreeService;

    public CommitCommand() {
        this.commitService = new CommitService();
        this.stateTreeService = new StateTreeService();
    }

    @Override
    public void run() {
        try {
            Session session = Neo4jSession.getSession();
            session.beginTransaction();

            String path = System.getProperty("user.dir");
            StateRoot stateRoot = stateTreeService.getStateRoot(path);
            NinjaNode currentNinjaNode = stateRoot.getCurrentNinjaNode();
            commitService.save(message, currentNinjaNode.getNextCommit());

            Branch branch = stateRoot.getCurrentBranch();
            branch.setLastCommit(currentNinjaNode.getNextCommit());
            new BranchRepository().save(branch);

            stateTreeService.addChangesToStateTree(currentNinjaNode.getNextCommit().getCommitTreeRoot(), stateRoot);

            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.COMMIT_SUCCESSFULLY));

            session.getTransaction().commit();
            Neo4jSession.closeSession();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}