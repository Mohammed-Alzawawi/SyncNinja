package org.syncninja.command;

import org.syncninja.model.Commit;
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
            Commit newCommit = commitService.save(message, stateRoot.getCurrentCommit());
            stateRoot.setCurrentCommit(newCommit);
            // ^ needs to update state root in db ^
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}