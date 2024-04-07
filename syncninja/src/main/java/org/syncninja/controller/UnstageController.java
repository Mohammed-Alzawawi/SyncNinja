package org.syncninja.controller;

import org.neo4j.ogm.session.Session;
import org.syncninja.service.CommitTreeService;
import org.syncninja.util.Neo4jSession;

import java.util.List;

public class UnstageController {
    private final CommitTreeService commitTreeService;

    public UnstageController() {
        this.commitTreeService = new CommitTreeService();
    }

    public void run(String path, List<String> filesToUnstage) throws Exception {
        Session session = Neo4jSession.getSession();
        session.beginTransaction();

        commitTreeService.unstage(path,filesToUnstage);

        session.getTransaction().commit();
        Neo4jSession.closeSession();
    }
}