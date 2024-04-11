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
        return commitRepository.save(commit);
    }

    public void save(String message, Commit commitTreeRoot) throws Exception {
        if (commitTreeRoot == null) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.STAGE_AREA_IS_EMPTY));
        }
        commitTreeRoot.setCommitted(true);
        commitTreeRoot.setMessage(message);
        commitTreeRoot.setNextCommit(createStagedCommit());
        stateTreeService.updateStateRoot(stateTreeService.getStateRoot(commitTreeRoot.getCommitTreeRoot().getFullPath()), commitTreeRoot);
        commitRepository.save(commitTreeRoot);
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