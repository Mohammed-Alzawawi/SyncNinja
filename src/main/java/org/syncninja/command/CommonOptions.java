package org.syncninja.command;

import picocli.CommandLine;

public class CommonOptions {
    @CommandLine.Option(names = {"-directory"}, paramLabel = "message", description = "Enter a message for the commit", required = true)
    public String directory;
    @CommandLine.Option(names = {"-id"}, paramLabel = "message", description = "Enter a message for the commit", required = true)
    public String sessionId;
}
