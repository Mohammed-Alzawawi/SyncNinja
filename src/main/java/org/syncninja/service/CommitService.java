package org.syncninja.service;

import org.syncninja.model.Commit;
import org.syncninja.repository.CommitRepository;

import java.util.ArrayList;

public class CommitService {
    private final CommitRepository commitRepository;

    public CommitService() {
        this.commitRepository = new CommitRepository();
    }

    public Commit createStagedCommit(){
        Commit commit = new Commit();
        commit.setCommitted(false);
        commit.setCommitTree(new ArrayList<>());
        return commitRepository.save(commit);
    }

    public Commit save(String message, Commit commit) {
        commit.setCommitted(true);
        commit.setMessage(message);
        commit.setNextCommit(createStagedCommit());
        return commitRepository.save(commit);
    }
}