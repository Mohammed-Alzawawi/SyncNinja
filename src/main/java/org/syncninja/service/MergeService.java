package org.syncninja.service;

import org.syncninja.model.Branch;
import org.syncninja.model.Commit;
import org.syncninja.model.committree.CommitDirectory;
import org.syncninja.model.committree.CommitFile;
import org.syncninja.model.committree.CommitNode;
import org.syncninja.model.committree.mergetree.MergeCommit;
import org.syncninja.model.committree.mergetree.MergeDirectory;
import org.syncninja.model.committree.mergetree.MergeFile;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.BranchRepository;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.repository.CommitRepository;
import org.syncninja.util.ResourceBundleEnum;

import java.util.List;
import java.util.Optional;

public class MergeService {
    private final BranchRepository branchRepository;
    private final StateTreeService stateTreeService;
    private final CommitRepository commitRepository;
    private final CommitNodeRepository commitNodeRepository;

    public MergeService() {
        this.branchRepository = new BranchRepository();
        this.stateTreeService = new StateTreeService();
        this.commitRepository = new CommitRepository();
        this.commitNodeRepository = new CommitNodeRepository();
    }

    public void merge(String path, String branchName) throws Exception {
        StateRoot stateRoot = stateTreeService.getStateRoot(path);
        Optional<Branch> branchOptional = branchRepository.findByName(branchName, path);

        if (branchOptional.isPresent()) {
            Branch branch = stateRoot.getCurrentBranch();
            Commit currentNinjaNode = branchOptional.get().getNextCommit();

            while(currentNinjaNode.isCommitted()){
                Commit currentBranchLastCommit = stateRoot.getCurrentCommit();
                MergeCommit mergeCommit = new MergeCommit(currentNinjaNode);
                MergeDirectory mergeTreeRoot = new MergeDirectory(currentNinjaNode.getCommitTreeRoot());
                mergeCommit.setCommitTree(mergeTreeRoot);
                mergeCommit.setNextCommit(currentBranchLastCommit.getNextCommit());
                currentBranchLastCommit.setNextCommit(mergeCommit);

                copyMergeCommitTree(currentNinjaNode.getCommitTreeRoot(), mergeTreeRoot, stateRoot);
                commitRepository.save(currentBranchLastCommit);
                stateTreeService.updateStateRoot(stateRoot, mergeCommit);
                branch.setLastCommit(mergeCommit);
                new BranchRepository().save(branch);

                currentNinjaNode = currentNinjaNode.getNextCommit();
            }

        } else {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_NOT_FOUND, new Object[]{branchName}));
        }
    }

    private void copyMergeCommitTree(CommitDirectory commitDirectory, MergeDirectory mergeDirectory, StateRoot stateRoot) {
        List<CommitNode> commitNodeList = commitDirectory.getCommitNodeList();

        for(CommitNode commitNode : commitNodeList){
            CommitNode currentMergeNode;
            if(commitNode instanceof CommitFile){
                currentMergeNode = new MergeFile((CommitFile) commitNode);
            } else {
                currentMergeNode = new MergeDirectory((CommitDirectory) commitNode);
                copyMergeCommitTree((CommitDirectory) commitNode, (MergeDirectory) currentMergeNode, stateRoot);
            }
            mergeDirectory.getCommitNodeList().add(currentMergeNode);
        }



        if (stateRoot.getPath().equals(commitDirectory.getFullPath())){
            commitNodeRepository.save(mergeDirectory);
        }
    }
}
