package org.syncninja.controller;

import org.syncninja.service.StateTreeService;
import org.syncninja.util.Neo4jSession;

import java.util.List;

public class RestoreController {
    private final StateTreeService stateTreeService;

    public RestoreController() {
        stateTreeService = new StateTreeService();
    }

    public void run(String path, List<String> listOfFilesToRestore) throws Exception {
        stateTreeService.restore(listOfFilesToRestore, path);
        Neo4jSession.closeSession();
    }
}