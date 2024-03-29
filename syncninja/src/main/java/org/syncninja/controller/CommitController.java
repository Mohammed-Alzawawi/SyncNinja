package org.syncninja.controller;

import org.syncninja.util.OutputCollector;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.service.CommitService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;

import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;

public class CommitController {

    private final CommitService commitService;
    private final StateTreeService stateTreeService;

    public CommitController() {
        this.commitService = new CommitService();
        this.stateTreeService = new StateTreeService();
    }

    public void run(String message, String path) {
        try {
            StateRoot stateRoot = stateTreeService.getStateRoot(path);
            NinjaNode currentNinjaNode = stateRoot.getCurrentNinjaNode();
            commitService.save(message, currentNinjaNode.getNextCommit());
            stateTreeService.addChangesToStateTree(
                    currentNinjaNode.getNextCommit().getCommitTreeRoot(),
                    null);
            OutputCollector.addString(ResourceMessagingService.getMessage(ResourceBundleEnum.COMMIT_SUCCESSFULLY));
            Neo4jSession.closeSession();
        } catch (Exception e) {
            OutputCollector.addString(e.getMessage());
        }
    }
}