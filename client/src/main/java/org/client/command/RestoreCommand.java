package org.client.command;

import org.syncninja.util.OutputCollector;
import org.syncninja.controller.RestoreController;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "restore")
public class RestoreCommand implements Runnable {
    private final RestoreController restoreController;

    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files to restore")
    private final List<String> listOfFilesToRestore = new ArrayList<>();

    public RestoreCommand() {
        this.restoreController = new RestoreController();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        restoreController.run(path, listOfFilesToRestore);
        System.out.println(OutputCollector.getString());
        OutputCollector.refresh();
    }
}