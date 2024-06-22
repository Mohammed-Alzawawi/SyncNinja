package org.syncninja.service;

import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.committree.CommitDirectory;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.CommitRepository;
import org.syncninja.util.ResourceBundleEnum;

public class CommitService {
    private final CommitRepository commitRepository;
    private final StateTreeService stateTreeService;

    public CommitService() {
        this.commitRepository = new CommitRepository();
        this.stateTreeService = new StateTreeService();
    }

    public Commit createStagedCommit() {
        Commit commit = new Commit();
        commit.setCommitted(false);
        commitRepository.save(commit);
        return commit;
    }

    public void save(String message, Commit commit) throws Exception {
        if (commit.getCommitTreeRoot() == null) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.STAGE_AREA_IS_EMPTY));
        }
        commit.setCommitted(true);
        commit.setMessage(message);
        commit.setNextCommit(createStagedCommit());
        stateTreeService.updateStateRoot(stateTreeService.getStateRoot(commit.getCommitTreeRoot().getFullPath()), commit);
        commitRepository.save(commit);
    }

    public void addCommitTreeRoot(CommitDirectory commitDirectory) throws Exception {
        String mainDirectoryPath = System.getProperty("user.dir");
        StateRoot stateRoot = stateTreeService.getStateRoot(mainDirectoryPath);
        NinjaNode currentNinjaNode = stateRoot.getCurrentNinjaNode();

        Commit commit = currentNinjaNode.getNextCommit();
        commit.setCommitTree(commitDirectory);
        commitRepository.save(commit);
    }
}
