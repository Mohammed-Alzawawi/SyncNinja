package org.syncninja.controller;

import org.syncninja.model.Branch;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.service.CommitService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.Neo4jSession;

public class CommitController {

    private final CommitService commitService;
    private final StateTreeService stateTreeService;

    public CommitController() {
        this.commitService = new CommitService();
        this.stateTreeService = new StateTreeService();
    }

    public void run(String path, String message) throws Exception {
        StateRoot stateRoot = stateTreeService.getStateRoot(path);
        NinjaNode currentNinjaNode = stateRoot.getCurrentNinjaNode();
        commitService.save(message, currentNinjaNode.getNextCommit());

        Branch branch = stateRoot.getCurrentBranch();
        branch.setLastCommit(currentNinjaNode.getNextCommit());

        stateTreeService.addChangesToStateTree(
                currentNinjaNode.getNextCommit().getCommitTreeRoot(),
                null);
        Neo4jSession.closeSession();
    }
}