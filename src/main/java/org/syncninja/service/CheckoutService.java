package org.syncninja.service;

import org.neo4j.ogm.model.Result;
import org.syncninja.dto.FileStatusEnum;
import org.syncninja.model.Branch;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.statetree.StateNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.BranchRepository;
import org.syncninja.repository.NinjaNodeRepository;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.CommitContainer;
import org.syncninja.util.ResourceBundleEnum;

import java.util.*;

public class CheckoutService {
    private final BranchRepository branchRepository;
    private final StateTreeService stateTreeService;
    private final StateTreeRepository stateTreeRepository;
    private final CommitService commitService;
    private final NinjaNodeRepository ninjaNodeRepository;
    private final PathFinderService pathFinderService;


    public CheckoutService() {
        this.branchRepository = new BranchRepository();
        this.commitService = new CommitService();
        this.stateTreeService = new StateTreeService();
        this.stateTreeRepository = new StateTreeRepository();
        this.ninjaNodeRepository = new NinjaNodeRepository();
        this.pathFinderService = new PathFinderService();

    }

    public void createNewBranch(String branchName, String path) throws Exception {
        if (branchRepository.findByName(branchName, path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_NAME_EXISTS, new Object[]{branchName}));
        }
        Branch newBranch = new Branch(branchName);
        newBranch.setNextCommit(commitService.createStagedCommit());
        StateRoot stateRoot = stateTreeService.getStateRoot(path);
        linkNewBranchWithNinjaNode(stateRoot, newBranch);
        updateStateRootWithNewBranch(stateRoot, newBranch);
    }

    public void updateStateRootWithNewBranch(StateRoot stateRoot, Branch newBranch) {
        stateRoot.setCurrentBranch(newBranch);
        stateRoot.setCurrentCommit(null);
        stateTreeRepository.save(stateRoot);
    }

    public void linkNewBranchWithNinjaNode(StateRoot stateRoot, Branch newBranch) {
        NinjaNode ninjaNode = stateRoot.getCurrentNinjaNode();
        ninjaNode.getBranchList().add(newBranch);
        ninjaNodeRepository.save(ninjaNode);
    }

    public void checkout(String branchName, String path) throws Exception {
        StateRoot stateRoot = stateTreeService.getStateRoot(path);
        Optional<Branch> branchOptional = branchRepository.findByName(branchName, path);
        if (branchOptional.isPresent()) {
            Branch branch = branchOptional.get();
            // get both sides of the path
            NinjaNode currentNode = stateRoot.getCurrentNinjaNode();
            NinjaNode targetNode = branch.getLastNinjaNode();
            Result result = branchRepository.getPathOfNinjaNodes(currentNode, targetNode).get();

            // get the relationships and nodes in the path
            Map<String, String>[] relationships = pathFinderService.getRelationshipsInPath(result);
            ArrayList<NinjaNode> ninjaNodesInPath = pathFinderService.getNinjaNodesInPath(result);

            //use these arrays to update the file system and the state tree and they are sorted
            CommitContainer commitContainer = new CommitContainer();
            ArrayList<NinjaNode> addedCommits = commitContainer.getCommitsToAdd();
            ArrayList<NinjaNode> removedCommits = commitContainer.getCommitsToRemove();

            // start the checkout logic
            NinjaNode ancestorNode = pathFinderService.getAncestorNode(relationships, ninjaNodesInPath);
            pathFinderService.findOutOfAncestor(addedCommits, ancestorNode, ninjaNodesInPath, relationships);
            pathFinderService.findGoingToAncestor(removedCommits, ancestorNode, ninjaNodesInPath, relationships);
            //now the arrays have the list of commits to add and remove

            //updating stateTree
            Map<String, StateNode> stateTree = stateTreeService.getStateTree(stateRoot);
            Map<StateNode, FileStatusEnum> fileStateMap = new HashMap<>();
            pathFinderService.updateStateTreeByRemovingCommits(stateTree, removedCommits, fileStateMap);
            pathFinderService.updateStateTreeByAddingCommits(stateTree, addedCommits, fileStateMap);
            stateTreeRepository.updateStateRoot(stateRoot, branch);
            pathFinderService.reflectStateTreeOnFileSystem(fileStateMap);
        } else {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_NOT_FOUND, new Object[]{branchName}));
        }
    }
}
