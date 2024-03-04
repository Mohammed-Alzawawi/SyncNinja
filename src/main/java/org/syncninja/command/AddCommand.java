package org.syncninja.command;

import org.syncninja.service.CommitTreeService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

@CommandLine.Command(name = "add")
public class AddCommand implements Runnable {
    private CommitTreeService commitTreeService;


    public AddCommand() {
        this.commitTreeService = new CommitTreeService();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        try {
            commitTreeService.addFilesFromDirectoryToCommitTree(path);
            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_ADDED, new Object[]{}));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}