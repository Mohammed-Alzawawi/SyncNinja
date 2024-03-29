package org.client.command;

import org.syncninja.util.OutputCollector;
import org.syncninja.controller.StatusController;
import picocli.CommandLine;

@CommandLine.Command(name = "status")
public class StatusCommand implements Runnable {
    private final StatusController statusController;

    public StatusCommand() {
        this.statusController = new StatusController();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        statusController.run(path);
        System.out.println(OutputCollector.getString());
        OutputCollector.refresh();
    }
}
