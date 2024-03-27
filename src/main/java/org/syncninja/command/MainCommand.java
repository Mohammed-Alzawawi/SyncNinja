package org.syncninja.command;

import picocli.CommandLine;

@CommandLine.Command(name = "", subcommands = {
        InitCommand.class,
        StatusCommand.class,
        AddCommand.class,
        CommitCommand.class,
        UnstageCommand.class
})

public class MainCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("HELL");
    }
}