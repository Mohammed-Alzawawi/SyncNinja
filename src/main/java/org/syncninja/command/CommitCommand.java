package org.syncninja.command;

import org.syncninja.model.NinjaNode;
import org.syncninja.model.StateTree.StateRoot;
import org.syncninja.service.CommitService;
import org.syncninja.service.StateTreeService;

import picocli.CommandLine;

@CommandLine.Command(name = "commit")
public class CommitCommand implements Runnable {

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
        String path = System.getProperty("user.dir");
        try {
            StateRoot stateRoot = stateTreeService.getStateRoot(path);
            NinjaNode currentNinjaNode = stateRoot.getCurrentCommit();
            if (currentNinjaNode == null){
                currentNinjaNode = stateRoot.getCurrentBranch();
            }
            commitService.save(message, currentNinjaNode.getNextCommit());
            stateTreeService.updateStateTree(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}