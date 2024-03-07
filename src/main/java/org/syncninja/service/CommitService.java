package org.syncninja.service;

import org.syncninja.model.Commit;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.repository.CommitRepository;

public class CommitService {
    private final CommitRepository commitRepository;
    private final CommitTreeService commitTreeService;

    public CommitService() {
        this.commitRepository = new CommitRepository();
        this.commitTreeService = new CommitTreeService();
    }

    public void createCommit(String message, String path) throws Exception {
        Commit commit = new Commit(message);
        CommitDirectory commitTreeRoot = commitTreeService.getCommitTreeRoot(path);
        commit.getCommitTree().add(commitTreeRoot);
        commitRepository.save(commit);
    }
}