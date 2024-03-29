package org.client.command;

import org.syncninja.util.OutputCollector;
import org.syncninja.controller.UnstageController;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "unstage", description = "unstage staged changes")
public class UnstageCommand implements Runnable {
    private final UnstageController unstageController;

    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files to unstage")
    private final List<String> listOfFilesToRestore = new ArrayList<>();

    public UnstageCommand() {
        this.unstageController = new UnstageController();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        unstageController.run(path, listOfFilesToRestore);
        System.out.println(OutputCollector.getString());
        OutputCollector.refresh();
    }
}
