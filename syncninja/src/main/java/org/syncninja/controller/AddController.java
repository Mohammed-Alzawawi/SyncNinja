package org.syncninja.controller;

import org.syncninja.util.OutputCollector;
import org.syncninja.service.CommitTreeService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;

import java.util.List;

public class AddController {
    private final CommitTreeService commitTreeService;

    public AddController() {
        this.commitTreeService = new CommitTreeService();
    }

    public void run(String mainDirectoryPath, List<String> listOfFilesToAdd) {
        try {
            commitTreeService.addFileToCommitTree(mainDirectoryPath, listOfFilesToAdd);
            OutputCollector.addString(ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_ADDED, new Object[]{}));
            Neo4jSession.closeSession();
        } catch (Exception exception) {
            OutputCollector.addString(exception.getMessage());
        }
    }
}