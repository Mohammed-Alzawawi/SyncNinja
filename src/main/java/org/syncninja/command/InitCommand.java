package org.syncninja.command;

import org.syncninja.model.Directory;
import org.syncninja.service.DirectoryService;
import picocli.CommandLine;

@CommandLine.Command(name = "init")
public class InitCommand implements Runnable {
    private final DirectoryService directoryService = new DirectoryService();

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        try {
            Directory directory = directoryService.createDirectory(path);
            directoryService.createDirectoryMainBranch(directory, "main");
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
