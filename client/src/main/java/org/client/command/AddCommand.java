package org.client.command;

import org.syncninja.util.OutputCollector;
import org.syncninja.controller.AddController;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "add", description = "add changes to staging area")
public class AddCommand implements Runnable{
    private final AddController addController;

    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files")
    private final List<String> filesToAdd = new ArrayList<>();

    public AddCommand() {
        this.addController = new AddController();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        addController.run(path, filesToAdd);
        System.out.println(OutputCollector.getString());
        OutputCollector.refresh();
    }
}
