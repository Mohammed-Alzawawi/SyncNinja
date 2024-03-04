package org.syncninja.command;

import org.syncninja.model.Directory;
import org.syncninja.service.DirectoryService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

@CommandLine.Command(name = "init")
public class InitCommand implements Runnable {
    private final DirectoryService directoryService;
    private final StateTreeService stateTreeService;


    public InitCommand() {
        this.directoryService = new DirectoryService();
        this.stateTreeService = new StateTreeService();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        try {
            Directory directory = directoryService.createDirectory(path);
            directoryService.createDirectoryMainBranch(directory, "main");
            stateTreeService.generateStateRootNode(path, directory.getBranch());
            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_INITIALIZED_SUCCESSFULLY, new Object[]{path}));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
