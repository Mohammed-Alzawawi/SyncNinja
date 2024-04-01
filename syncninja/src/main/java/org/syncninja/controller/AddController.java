package org.syncninja.controller;

import org.syncninja.service.CommitTreeService;
import org.syncninja.util.Neo4jSession;

import java.util.List;

public class AddController {
    private final CommitTreeService commitTreeService;

    public AddController() {
        this.commitTreeService = new CommitTreeService();
    }

    public void run(String mainDirectoryPath, List<String> listOfFilesToAdd) throws Exception {
        commitTreeService.addFileToCommitTree(mainDirectoryPath, listOfFilesToAdd);
        Neo4jSession.closeSession();
    }
}