package org.syncninja.command;

import picocli.CommandLine;

@CommandLine.Command(name = "")
public class MainCommand implements Runnable{
    @Override
    public void run() {
        System.out.println("Welcome to the SyncNinja :)");
    }
}
