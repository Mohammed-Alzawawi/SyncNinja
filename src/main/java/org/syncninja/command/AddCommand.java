package org.syncninja.command;

import org.syncninja.OutputCollector;
import org.syncninja.service.CommitTreeService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "add", description = "Add files to the commit tree")
public class AddCommand extends CommonOptions implements Runnable {

    private final CommitTreeService commitTreeService;

    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files")
    private final List<String> listOfFilesToAdd = new ArrayList<>();

    public AddCommand() {
        this.commitTreeService = new CommitTreeService();
    }

    @Override
    public void run() {
        try {
            String mainDirectoryPath = directory;
            commitTreeService.addFileToCommitTree(mainDirectoryPath, listOfFilesToAdd);
            OutputCollector.addString(sessionId, ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_ADDED, new Object[]{}));
            Neo4jSession.closeSession();
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}