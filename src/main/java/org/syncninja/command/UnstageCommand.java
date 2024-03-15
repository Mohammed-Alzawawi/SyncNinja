package org.syncninja.command;

import org.syncninja.service.CommitTreeService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "unstage", description = "Unstage files from the staging area")
public class UnstageCommand implements Runnable {
    private final CommitTreeService commitTreeService;

    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files to unstage from the staging area")
    private List<String> filesToUnstage = new ArrayList<>();

    public UnstageCommand() {
        this.commitTreeService=new CommitTreeService();
    }
    @Override
    public void run() {
        try {
            String mainDirectoryPath = System.getProperty("user.dir");
            commitTreeService.unstage(mainDirectoryPath,filesToUnstage);
            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_REMOVED, new Object[]{}));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
