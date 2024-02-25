package org.syncninja;

import org.syncninja.command.MainCommand;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        int exitCode = new CommandLine(new MainCommand()).execute(args);
        long endtime = System.currentTimeMillis();
        System.out.println((endtime - startTime) + " MS") ;
        System.exit(exitCode);
    }
}