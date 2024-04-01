package org.syncninja.controller;

import org.syncninja.service.CommitTreeService;
import org.syncninja.util.Neo4jSession;

import java.util.List;

public class UnstageController {
    private final CommitTreeService commitTreeService;

    public UnstageController() {
        this.commitTreeService = new CommitTreeService();
    }

    public void run(String path, List<String> filesToUnstage) throws Exception {
        commitTreeService.unstage(path,filesToUnstage);
        Neo4jSession.closeSession();
    }
}