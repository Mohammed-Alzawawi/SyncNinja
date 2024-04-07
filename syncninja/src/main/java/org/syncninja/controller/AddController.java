package org.syncninja.controller;

import org.neo4j.ogm.session.Session;
import org.syncninja.service.CommitTreeService;
import org.syncninja.util.Neo4jSession;

import java.util.List;

public class AddController {
    private final CommitTreeService commitTreeService;

    public AddController() {
        this.commitTreeService = new CommitTreeService();
    }

    public void run(String mainDirectoryPath, List<String> listOfFilesToAdd) throws Exception {
        Session session = Neo4jSession.getSession();
        session.beginTransaction();
        commitTreeService.addFileToCommitTree(mainDirectoryPath, listOfFilesToAdd);
        session.getTransaction().commit();
        Neo4jSession.closeSession();
    }
}