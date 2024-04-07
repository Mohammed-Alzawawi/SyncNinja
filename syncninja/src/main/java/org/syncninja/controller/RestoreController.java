package org.syncninja.controller;

import org.neo4j.ogm.session.Session;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.Neo4jSession;

import java.util.List;

public class RestoreController {
    private final StateTreeService stateTreeService;

    public RestoreController() {
        stateTreeService = new StateTreeService();
    }

    public void run(String path, List<String> listOfFilesToRestore) throws Exception {
        Session session = Neo4jSession.getSession();
        session.beginTransaction();

        stateTreeService.restore(listOfFilesToRestore, path);

        session.getTransaction().commit();
        Neo4jSession.closeSession();
    }
}