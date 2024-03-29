package org.client.command;

import org.syncninja.util.OutputCollector;
import org.syncninja.controller.InitController;
import picocli.CommandLine;

@CommandLine.Command(name = "init", description = "Initialize directory")
public class InitCommand implements Runnable {
    private final InitController initController;

    public InitCommand() {
        this.initController = new InitController();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        initController.run(path);
        System.out.println(OutputCollector.getString());
        OutputCollector.refresh();
    }
}