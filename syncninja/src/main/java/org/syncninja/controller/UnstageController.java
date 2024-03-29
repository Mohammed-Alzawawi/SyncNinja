package org.syncninja.controller;

import org.syncninja.util.OutputCollector;
import org.syncninja.service.CommitTreeService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;

import java.util.List;

public class UnstageController {
    private final CommitTreeService commitTreeService;

    public UnstageController() {
        this.commitTreeService = new CommitTreeService();
    }

    public void run(String path, List<String> filesToUnstage) {
        try {
            commitTreeService.unstage(path,filesToUnstage);
            OutputCollector.addString(ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_REMOVED, new Object[]{}));
            Neo4jSession.closeSession();
        } catch (Exception exception) {
            OutputCollector.addString(exception.getMessage());
        }
    }
}