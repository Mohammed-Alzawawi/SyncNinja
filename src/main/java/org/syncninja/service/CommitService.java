package org.syncninja.service;

import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.committree.CommitDirectory;
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
        stateTreeService.updateStateRoot(stateTreeService.getStateRoot(commitTreeRoot.getCommitTreeRoot().getPath()), commitTreeRoot);
        commitRepository.save(commitTreeRoot);
    }

    public void addCommitTreeRoot(CommitDirectory commitDirectory) throws Exception {
        NinjaNode currentNinjaNode = stateTreeService.getStateRoot(commitDirectory.getPath()).getCurrentCommit();
        if (currentNinjaNode == null) {
            currentNinjaNode = stateTreeService.getStateRoot(commitDirectory.getPath()).getCurrentBranch();
        }
        Commit commit = currentNinjaNode.getNextCommit();
        commit.setCommitTree(commitDirectory);
        commitRepository.save(commit);
    }
}