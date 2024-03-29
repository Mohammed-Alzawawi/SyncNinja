package org.client.command;

import org.syncninja.util.OutputCollector;
import org.syncninja.controller.CheckoutController;
import picocli.CommandLine;

@CommandLine.Command(name = "checkout")
public class CheckoutCommand implements Runnable{
    private final CheckoutController checkOutController;

    @CommandLine.Parameters(paramLabel = "branch name", description = "name your branch")
    private String branchName;

    @CommandLine.Option(names = {"-b"}, paramLabel = "new branch name")
    private boolean isNewBranch;

    public CheckoutCommand() {
        this.checkOutController = new CheckoutController();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        checkOutController.run(path, branchName, isNewBranch);
        System.out.println(OutputCollector.getString());
        OutputCollector.refresh();
    }
}