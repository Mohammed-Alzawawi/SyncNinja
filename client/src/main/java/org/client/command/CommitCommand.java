package org.client.command;

import org.syncninja.util.OutputCollector;
import org.syncninja.controller.CommitController;

import picocli.CommandLine;

@CommandLine.Command(name = "commit")
public class CommitCommand implements Runnable {

    @CommandLine.Option(names = {"-m"}, paramLabel = "message", description = "Enter a message for the commit", required = true)
    private String message;

    private final CommitController commitController;

    public CommitCommand() {
        this.commitController = new CommitController();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        commitController.run(message, path);
        System.out.println(OutputCollector.getString());
        OutputCollector.refresh();
    }
}