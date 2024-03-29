package org.syncninja.controller;

import org.syncninja.util.OutputCollector;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;

import java.util.List;

public class RestoreController {
    private final StateTreeService stateTreeService;

    public RestoreController() {
        stateTreeService = new StateTreeService();
    }

    public void run(String path, List<String> listOfFilesToRestore) {
        try{
            stateTreeService.restore(listOfFilesToRestore, path);
            OutputCollector.addString(ResourceMessagingService.getMessage(ResourceBundleEnum.RESTORED_SUCCESSFULLY));
            Neo4jSession.closeSession();
        } catch (Exception e){
            OutputCollector.addString(e.getMessage());
        }
    }
}