package org.syncninja.command;

import org.syncninja.service.CommitService;
import picocli.CommandLine;

@CommandLine.Command(name = "commit")
public class CommitCommand implements Runnable {

    @CommandLine.Option(names = {"-m"}, paramLabel = "message", description = "Enter a message for the commit", required = true)
    private String message;

    private final CommitService commitService;

    public CommitCommand() {
        this.commitService = new CommitService();
    }

    @Override
    public void run() {
        commitService.createCommit(message);
    }
}