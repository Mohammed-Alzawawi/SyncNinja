package org.syncninja.command;

import picocli.CommandLine;

@CommandLine.Command(name = "",
        subcommands = {StatusCommand.class})
public class MainCommand implements Runnable{
    @Override
    public void run() {
        System.out.println("Welcome to the SyncNinja :)");
    }
}
