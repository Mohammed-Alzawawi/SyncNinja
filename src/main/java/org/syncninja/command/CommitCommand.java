package org.syncninja.command;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.Branch;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.NinjaNodeRepository;
import org.syncninja.service.CommitService;
import org.syncninja.service.StateTreeService;

import org.syncninja.util.Neo4jSession;
import picocli.CommandLine;

@CommandLine.Command(name = "commit")
public class CommitCommand implements Runnable {

    @CommandLine.Option(names = {"-m"}, paramLabel = "message", description = "Enter a message for the commit", required = true)
    private String message;

    private final CommitService commitService;
    private final StateTreeService stateTreeService;
    private final NinjaNodeRepository ninjaNodeRepository;

    public CommitCommand() {
        this.commitService = new CommitService();
        this.stateTreeService = new StateTreeService();
        this.ninjaNodeRepository = new NinjaNodeRepository();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        try {
            StateRoot stateRoot = stateTreeService.getStateRoot(path);
            NinjaNode currentNinjaNode = stateRoot.getCurrentNinjaNode();
            commitService.save(message, currentNinjaNode.getNextCommit());
            Branch branch = stateRoot.getCurrentBranch();
            branch.setLastCommit(currentNinjaNode.getNextCommit());
            ninjaNodeRepository.save(branch);
            stateTreeService.addChangesToStateTree(
                    currentNinjaNode.getNextCommit().getCommitTreeRoot(), null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}