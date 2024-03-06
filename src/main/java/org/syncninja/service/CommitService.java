package org.syncninja.service;

import org.syncninja.model.Commit;
import org.syncninja.repository.CommitRepository;

public class CommitService {
    private final CommitRepository commitRepository;

    public CommitService() {
        this.commitRepository = new CommitRepository();
    }

    public void createCommit(String message){
        commitRepository.save(new Commit(message));
    }
}
