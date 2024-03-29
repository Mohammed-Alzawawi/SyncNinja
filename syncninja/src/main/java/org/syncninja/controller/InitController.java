package org.syncninja.controller;

import org.syncninja.util.OutputCollector;
import org.syncninja.model.Branch;
import org.syncninja.model.Directory;
import org.syncninja.service.CommitService;
import org.syncninja.service.DirectoryService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;

public class InitController {
    private final DirectoryService directoryService;
    private final StateTreeService stateTreeService;
    private final CommitService commitService;


    public InitController() {
        this.directoryService = new DirectoryService();
        this.stateTreeService = new StateTreeService();
        this.commitService = new CommitService();
    }

    public void run(String path) {
        try {
            Directory directory = directoryService.createDirectory(path);
            // creating main branch
            directoryService.createDirectoryMainBranch(directory, "main");
            Branch mainBranch = directory.getBranch();
            // creating staging area
            mainBranch.setNextCommit(commitService.createStagedCommit());
            // creating state tree
            stateTreeService.generateStateRootNode(path, mainBranch);
            OutputCollector.addString(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_INITIALIZED_SUCCESSFULLY, new Object[]{path}));
            Neo4jSession.closeSession();
        } catch (Exception exception) {
            OutputCollector.addString(exception.getMessage());
        }
    }
}