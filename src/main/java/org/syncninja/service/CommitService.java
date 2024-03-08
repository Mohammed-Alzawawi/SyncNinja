package org.syncninja.service;

import org.syncninja.model.Commit;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.CommitRepository;

import java.util.ArrayList;

public class CommitService {
    private final CommitRepository commitRepository;
    private final StateTreeService stateTreeService;

    public CommitService() {
        this.commitRepository = new CommitRepository();
        this.stateTreeService = new StateTreeService();
    }

    public Commit createStagedCommit(){
        Commit commit = new Commit();
        commit.setCommitted(false);
        return commitRepository.save(commit);
    }

    public Commit save(String message, Commit commit) {
        commit.setCommitted(true);
        commit.setMessage(message);
        commit.setNextCommit(createStagedCommit());
        return commitRepository.save(commit);
    }

    public void addCommitTree(String path, CommitDirectory commitDirectory) throws Exception {
        Commit commit = stateTreeService.getStateRoot(path).getCurrentCommit();
        commit.setCommitTree(commitDirectory);
        commitRepository.save(commit);
    }
}