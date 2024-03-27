package org.syncninja.command;

import org.syncninja.model.NinjaNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.service.CommitService;
import org.syncninja.service.StateTreeService;

import picocli.CommandLine;

@CommandLine.Command(name = "commit")
public class CommitCommand extends CommonOptions implements Runnable {

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
        String path = directory;
        try {
            long startTime = System.currentTimeMillis();
            StateRoot stateRoot = stateTreeService.getStateRoot(path);
            NinjaNode currentNinjaNode = stateRoot.getCurrentNinjaNode();
            commitService.save(message, currentNinjaNode.getNextCommit());
            stateTreeService.addChangesToStateTree(
                    currentNinjaNode.getNextCommit().getCommitTreeRoot(),
                    stateRoot,
                    null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}