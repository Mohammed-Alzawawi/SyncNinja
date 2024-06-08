package org.syncninja.service;

import org.neo4j.ogm.model.Result;
import org.syncninja.dto.FileStatusEnum;
import org.syncninja.model.Branch;
import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.committree.CommitDirectory;
import org.syncninja.model.committree.CommitFile;
import org.syncninja.model.committree.CommitNode;
import org.syncninja.model.committree.mergetree.MergeCommit;
import org.syncninja.model.committree.mergetree.MergeDirectory;
import org.syncninja.model.committree.mergetree.MergeFile;
import org.syncninja.model.statetree.StateNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.BranchRepository;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.repository.CommitRepository;
import org.syncninja.util.CommitContainer;
import org.syncninja.util.ResourceBundleEnum;

import java.util.*;
import java.util.stream.Collectors;

public class MergeService {
    private final BranchRepository branchRepository;
    private final StateTreeService stateTreeService;
    private final CommitNodeRepository commitNodeRepository;
    private final PathFinderService pathFinderService;
    private final CommitRepository commitRepository;

    public MergeService() {
        this.branchRepository = new BranchRepository();
        this.stateTreeService = new StateTreeService();
        this.commitNodeRepository = new CommitNodeRepository();
        this.pathFinderService = new PathFinderService();
        this.commitRepository = new CommitRepository();
    }

    public void merge(String path, String branchName) throws Exception {
        StateRoot stateRoot = stateTreeService.getStateRoot(path);
        Optional<Branch> branchOptional = branchRepository.findByName(branchName, path);

        if (branchOptional.isPresent()) {
            Branch branch = branchOptional.get();
            if (!branch.getName().equals(stateRoot.getCurrentBranch().getName())){
                // get both sides of the path
                NinjaNode currentNode = stateRoot.getCurrentNinjaNode();
                NinjaNode targetNode = branch.getLastNinjaNode();
                Result result = branchRepository.getPathOfNinjaNodes(currentNode, targetNode).get();

                // get the relationships and nodes in the path
                Map<String, String>[] relationships = pathFinderService.getRelationshipsInPath(result);
                ArrayList<NinjaNode> ninjaNodesInPath = pathFinderService.getNinjaNodesInPath(result);

                // use these arrays to update the file system and the state tree and they are sorted
                CommitContainer commitContainer = new CommitContainer();
                ArrayList<NinjaNode> mergedNinjaNodes = commitContainer.getCommitsToAdd();
                ArrayList<NinjaNode> ignoredNinjaNodes = commitContainer.getCommitsToRemove();

                // get the nodes to merge and the ones to ignore
                NinjaNode ancestorNode = pathFinderService.getAncestorNode(relationships, ninjaNodesInPath);
                pathFinderService.findOutOfAncestor(mergedNinjaNodes, ancestorNode, ninjaNodesInPath, relationships);
                pathFinderService.findGoingToAncestor(ignoredNinjaNodes, ancestorNode, ninjaNodesInPath, relationships);

                Map<String, NinjaNode> ignoredNinjaNodeMap = ignoredNinjaNodes.stream().collect(Collectors.toMap(NinjaNode::getId, (ninjaNode -> ninjaNode)));

                // merge logic
                MergeCommit mergeCommit = null;
                for (NinjaNode ninjaNodeToMerge : mergedNinjaNodes) {
                    if (!ignoredNinjaNodeMap.containsKey(ninjaNodeToMerge.getId()) && ninjaNodeToMerge instanceof Commit) {
                        mergeCommit = new MergeCommit((Commit) ninjaNodeToMerge);
                        MergeDirectory mergeTreeRoot = new MergeDirectory(((Commit) ninjaNodeToMerge).getCommitTreeRoot());
                        mergeCommit.setCommitTree(mergeTreeRoot);
                        mergeCommit.setNextCommit(currentNode.getNextCommit());
                        currentNode.setNextCommit(mergeCommit);

                        copyMergeCommitTree(((Commit) ninjaNodeToMerge).getCommitTreeRoot(), mergeTreeRoot, stateRoot);
                        commitRepository.save((Commit) ninjaNodeToMerge);
                        currentNode = currentNode.getNextCommit();
                    }
                }

                if(mergeCommit != null){
                    // updating the branch
                    Branch currentBranch = stateRoot.getCurrentBranch();
                    currentBranch.setLastCommit(mergeCommit);
                    new BranchRepository().save(currentBranch);

                    // updating the state tree
                    Map<String, StateNode> stateTree = stateTreeService.getStateTree(stateRoot);
                    Map<StateNode, FileStatusEnum> fileStateMap = new HashMap<>();
                    pathFinderService.updateStateTreeByRemovingCommits(stateTree, ignoredNinjaNodes, fileStateMap);
                    pathFinderService.updateStateTreeByAddingCommits(stateTree, mergedNinjaNodes, fileStateMap);
                    pathFinderService.reflectStateTreeOnFileSystem(fileStateMap);

                    stateTreeService.updateStateRoot(stateRoot, mergeCommit);
                }
            } else {
                throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.YOU_ARE_ALREADY_IN_BRANCH, new Object[]{branchName}));
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
