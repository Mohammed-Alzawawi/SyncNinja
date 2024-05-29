package org.syncninja.command;

import picocli.CommandLine;

@CommandLine.Command(name = "", subcommands = {
        InitCommand.class,
        AddCommand.class,
        CommitCommand.class,
        RestoreCommand.class,
        StatusCommand.class,
        UnstageCommand.class,
        CheckoutCommand.class,
        MergeCommand.class
})
public class MainCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("welcome to SyncNinja :)");
    }
}