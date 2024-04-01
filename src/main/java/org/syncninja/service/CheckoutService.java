package org.syncninja.service;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.syncninja.model.Branch;
import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.BranchRepository;
import org.syncninja.repository.NinjaNodeRepository;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.CommitContainer;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    public NinjaNode getAncestorNode(Map<String, String>[] relationships, ArrayList<NinjaNode> ninjaNodes) {
        Map<String, Integer> occurenceOfEachNode = new HashMap<String, Integer>();
        for (Map<String, String> map : relationships) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getKey().equals("startNodeId")) {
                    if (occurenceOfEachNode.containsKey(entry.getValue())) {
                        int occurence = occurenceOfEachNode.get(entry.getValue()) + 1;
                        occurenceOfEachNode.put(entry.getValue(), occurence);
                    } else {
                        occurenceOfEachNode.put(entry.getValue(), 1);
                    }
                }
            }
        }
        String ancestorId = "";
        for (Map.Entry<String, Integer> node : occurenceOfEachNode.entrySet()) {
            if (node.getValue() == 2) {
                ancestorId = node.getKey();
                break;
            }
        }
        for (NinjaNode ninjaNode : ninjaNodes) {
            if (ninjaNode.getId().equals(ancestorId)) {
                return ninjaNode;
            }
        }
        return null;
    }

    public Map<String, String>[] getRelationshipsInPath(Result result) {
        @SuppressWarnings("unchecked")
        Map<String, String>[] relationships = (Map<String, String>[]) result.queryResults().iterator().next().get("relationships_on_path");
        return relationships;
    }

    public ArrayList<NinjaNode> getNinjaNodesInPath(Result result) {
        ArrayList<NinjaNode> ninjaNodes = (ArrayList<NinjaNode>) result.queryResults().iterator().next().get("nodes_on_path");
        return ninjaNodes;
    }

    public void commitsToAdd(ArrayList<NinjaNode> listOfCommitsToAdd, NinjaNode ancestorNode, ArrayList<NinjaNode> nodesInpath) {
        if (ancestorNode != null) {
            int indexOfAncestor = nodesInpath.indexOf(ancestorNode);
            for (int i = indexOfAncestor + 1; i < nodesInpath.size(); i++) {
                NinjaNode node = nodesInpath.get(i);
                if (node instanceof Commit) {
                    listOfCommitsToAdd.add(node);
                }
            }
        } else {
            //target branch is a child of the current branch that means all the commits in the path should be added
            if (nodesInpath.get(nodesInpath.size() - 1).getBranchList().size() == 0) {
                for (int i = 1; i < nodesInpath.size(); i++) {
                    NinjaNode node = nodesInpath.get(i);
                    if (node instanceof Commit) {
                        listOfCommitsToAdd.add(node);
                    }
                }
            }
        }
    }

    public void commitsToRemove(ArrayList<NinjaNode> listOfCommitsToRemove, NinjaNode ancestorNode, ArrayList<NinjaNode> nodesInpath) {
        if (ancestorNode != null) {
            int indexOfAncestor = nodesInpath.indexOf(ancestorNode);
            for (int i = 0; i < indexOfAncestor; i++) {
                NinjaNode node = nodesInpath.get(i);
                if (node instanceof Commit) {
                    listOfCommitsToRemove.add(node);
                }
            }
        } else {
            //target branch is the parent of the current branch that means all the commits in the path should be removed
            if (nodesInpath.get(nodesInpath.size() - 1).getBranchList().size() > 0) {
                for (int i = 0; i < nodesInpath.size() - 1; i++) {
                    NinjaNode node = nodesInpath.get(i);
                    if (node instanceof Commit) {
                        listOfCommitsToRemove.add(node);
                    }
                }
            }
        }
    }


    public void createNewBranch(String branchName, String path) throws Exception {
        if (branchRepository.findByName(branchName, path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_NAME_EXISTS, new Object[]{branchName}));
        }
        Session session = Neo4jSession.getSession();
        Branch newBranch = new Branch(branchName);
        newBranch.setNextCommit(commitService.createStagedCommit());
        StateRoot stateRoot = stateTreeService.getStateRoot(path);
        Branch currentBranch = stateRoot.getCurrentBranch();
        currentBranch.setLastCommit(stateRoot.getCurrentCommit());
        session.save(currentBranch);
        linkNewBranchWithNinjaNode(stateRoot, newBranch);
        updateStateRootWithNewBranch(stateRoot, newBranch);
    }

    public void updateStateRootWithNewBranch(StateRoot stateRoot, Branch newBranch) {
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
            Result result = branchRepository.getPathOfNinjaNodes(stateRoot.getCurrentCommit(), branch.getLastCommit()).get();
            Map<String, String>[] relationships = getRelationshipsInPath(result);
            ArrayList<NinjaNode> ninjaNodesInPath = getNinjaNodesInPath(result);
            CommitContainer commitContainer = new CommitContainer();
            //use these arrays to update the file system and the state tree and they are sorted
            ArrayList<NinjaNode> addedCommits = commitContainer.getCommitsToAdd();
            ArrayList<NinjaNode> removedCommits = commitContainer.getCommitsToRemove();
            NinjaNode ancestorNode = getAncestorNode(relationships, ninjaNodesInPath);
            commitsToAdd(addedCommits, ancestorNode, ninjaNodesInPath);
            commitsToRemove(removedCommits, ancestorNode, ninjaNodesInPath);
            //now the arrays have the list of commits to add and remove
            stateTreeRepository.updateStateRoot(stateRoot, branch);
        } else {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_NOT_FOUND, new Object[]{branchName}));
        }
    }
}