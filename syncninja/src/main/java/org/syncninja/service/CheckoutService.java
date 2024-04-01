package org.syncninja.service;

import org.syncninja.model.Branch;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.BranchRepository;
import org.syncninja.repository.NinjaNodeRepository;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.ResourceBundleEnum;

import java.util.Optional;

public class CheckoutService {
    private final BranchRepository branchRepository;
    private final StateTreeService stateTreeService;
    private final StateTreeRepository stateTreeRepository;
    private final CommitService commitService;
    private final NinjaNodeRepository ninjaNodeRepository;


    public CheckoutService() {
        this.branchRepository = new BranchRepository();
        this.commitService = new CommitService();
        this.stateTreeService = new StateTreeService();
        this.stateTreeRepository = new StateTreeRepository();
        this.ninjaNodeRepository = new NinjaNodeRepository();

    }

    public void createNewBranch(String branchName, String path) throws Exception{
        if (branchRepository.findByName(branchName, path).isPresent()){
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_NAME_EXISTS, new Object[]{branchName}));
        }
        Branch newBranch = new Branch(branchName);
        newBranch.setNextCommit(commitService.createStagedCommit());
        StateRoot stateRoot = stateTreeService.getStateRoot(path);
        linkNewBranchWithNinjaNode(stateRoot, newBranch);
        updateStateRootWithNewBranch(stateRoot, newBranch);
    }

    public void updateStateRootWithNewBranch(StateRoot stateRoot,Branch newBranch){
        stateRoot.setCurrentBranch(newBranch);
        stateRoot.setCurrentCommit(null);
        stateTreeRepository.save(stateRoot);

    }

    public void linkNewBranchWithNinjaNode(StateRoot stateRoot, Branch newBranch) throws Exception {
        NinjaNode ninjaNode = stateRoot.getCurrentNinjaNode();
        ninjaNode.getBranchList().add(newBranch);
        ninjaNodeRepository.save(ninjaNode);
    }

    public void checkout(String branchName, String path) throws Exception {
        StateRoot stateRoot = stateTreeService.getStateRoot(path);
        Optional<Branch> branchOptional = branchRepository.findByName(branchName, path);
        if (branchOptional.isPresent()) {
            Branch branch = branchOptional.get();
            stateTreeRepository.updateStateRoot(stateRoot, branch);
        } else {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_NOT_FOUND, new Object[]{branchName}));
        }
    }
}
