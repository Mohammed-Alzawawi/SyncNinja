package org.syncninja.controller;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.Branch;
import org.syncninja.model.Directory;
import org.syncninja.service.CommitService;
import org.syncninja.service.DirectoryService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.Neo4jSession;

public class InitController {
    private final DirectoryService directoryService;
    private final StateTreeService stateTreeService;
    private final CommitService commitService;

    public InitController() {
        this.directoryService = new DirectoryService();
        this.stateTreeService = new StateTreeService();
        this.commitService = new CommitService();
    }

    public void run(String path) throws Exception {
        Session session = Neo4jSession.getSession();
        session.beginTransaction();

        Directory directory = directoryService.createDirectory(path);
        // creating main branch
        directoryService.createDirectoryMainBranch(directory, "main");
        Branch mainBranch = directory.getBranch();
        // creating staging area
        mainBranch.setNextCommit(commitService.createStagedCommit());
        // creating state tree
        stateTreeService.generateStateRootNode(path, mainBranch);

        session.getTransaction().commit();
        Neo4jSession.closeSession();
    }
}