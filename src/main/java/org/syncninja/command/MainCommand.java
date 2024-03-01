package org.syncninja.command;

import picocli.CommandLine;

@CommandLine.Command(name = "", subcommands = InitCommand.class)
public class MainCommand implements Runnable{
    @Override
    public void run() {
        System.out.println("test test");
    }
}
