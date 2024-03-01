package org.syncninja;

import org.syncninja.command.MainCommand;
import org.syncninja.util.CompareFileUtil;
import picocli.CommandLine;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new MainCommand()).execute(args);
        System.exit(exitCode);
    }
}