package org.client;

import org.client.command.MainCommand;
import picocli.CommandLine;

public class Client {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MainCommand()).execute(args);
        System.exit(exitCode);
    }
}