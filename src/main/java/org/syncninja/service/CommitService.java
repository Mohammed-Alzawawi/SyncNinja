package org.syncninja.service;

import org.syncninja.model.Commit;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.repository.CommitRepository;

public class CommitService {
    private final CommitRepository commitRepository;

    public CommitService() {
        this.commitRepository = new CommitRepository();
    }

    public void createCommit(String message, String path) throws Exception {
        Commit commit = new Commit(message);
        commitRepository.save(commit);
    }
}