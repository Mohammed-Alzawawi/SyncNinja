package org.syncninja.service;

import org.neo4j.ogm.model.Result;
import org.syncninja.dto.FileStatusEnum;
import org.syncninja.model.Branch;
import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.committree.CommitDirectory;
import org.syncninja.model.committree.CommitFile;
import org.syncninja.model.committree.CommitNode;
import org.syncninja.model.statetree.StateDirectory;
import org.syncninja.model.statetree.StateFile;
import org.syncninja.model.statetree.StateNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.BranchRepository;
import org.syncninja.repository.NinjaNodeRepository;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.CommitContainer;
import org.syncninja.util.ResourceBundleEnum;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
        Map<String, Integer>occurenceOfEachNode = new HashMap<String, Integer>();
        String ancestorId = null;
        for (Map<String, String> map : relationships) {
            String startNodeId = map.get("startNodeId");
            occurenceOfEachNode.put(startNodeId, occurenceOfEachNode.getOrDefault(startNodeId, 0) + 1);
            if (occurenceOfEachNode.getOrDefault(startNodeId, 0) == 2) {
                ancestorId = startNodeId;
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

    public void commitsToAdd(ArrayList<NinjaNode> listOfCommitsToAdd, NinjaNode ancestorNode, ArrayList<NinjaNode> nodesInpath, Map<String, String>[] relationships) {
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
            //use relationship array to see if its startNode or Endnode
            if (relationships[0].get("startNodeId").equals(nodesInpath.get(0).getId())) {
                for (int i = 1; i < nodesInpath.size(); i++) {
                    NinjaNode node = nodesInpath.get(i);
                    if (node instanceof Commit) {
                        listOfCommitsToAdd.add(node);
                    }
                }
            }
        }
    }

    public void commitsToRemove(ArrayList<NinjaNode> listOfCommitsToRemove, NinjaNode ancestorNode, ArrayList<NinjaNode> nodesInpath, Map<String, String>[] relationships) {
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
            if (relationships[0].get("endNodeId").equals(nodesInpath.get(0).getId())) {
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
            // get both sides of the path
            NinjaNode currentNode = stateRoot.getCurrentNinjaNode();
            NinjaNode targetNode = branch.getLastNinjaNode();
            Result result = branchRepository.getPathOfNinjaNodes(currentNode, targetNode).get();

            // get the relationships and nodes in the path
            Map<String, String>[] relationships = getRelationshipsInPath(result);
            ArrayList<NinjaNode> ninjaNodesInPath = getNinjaNodesInPath(result);


            //use these arrays to update the file system and the state tree and they are sorted
            CommitContainer commitContainer = new CommitContainer();
            ArrayList<NinjaNode> addedCommits = commitContainer.getCommitsToAdd();
            ArrayList<NinjaNode> removedCommits = commitContainer.getCommitsToRemove();

            // start the checkout logic
            NinjaNode ancestorNode = getAncestorNode(relationships, ninjaNodesInPath);
            commitsToAdd(addedCommits, ancestorNode, ninjaNodesInPath, relationships);
            commitsToRemove(removedCommits, ancestorNode, ninjaNodesInPath, relationships);
            //now the arrays have the list of commits to add and remove

            //updating stateTree
            Map<String, StateNode> stateTree = stateTreeService.getStateTree(path);
            Map<StateNode, FileStatusEnum> fileStateMap = new HashMap<>();
            updateStateTreeByRemovingCommits(stateTree, removedCommits, fileStateMap);//
            updateStateTreeByAddingCommits(stateTree, addedCommits, fileStateMap);//
            stateTreeRepository.updateStateRoot(stateRoot, branch);
            reflectStateTreeOnFileSystem(fileStateMap); //
        } else {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_NOT_FOUND, new Object[]{branchName}));
        }
    }

    private void updateStateTreeByAddingCommits(Map<String, StateNode> stateTree, ArrayList<NinjaNode> addedCommits, Map<StateNode, FileStatusEnum> fileStateMap ) throws Exception {
        for (NinjaNode ninjaNode : addedCommits) {
            Commit commit = (Commit) ninjaNode;
            CommitDirectory commitDirectory = commit.getCommitTreeRoot();
            Set<StateNode> toBeDeleted = new HashSet<>();
            addCommitChanges(commitDirectory, stateTree, toBeDeleted, fileStateMap); //
            stateTreeRepository.deleteNodeList(toBeDeleted);
        }
    }

    public void addCommitChanges(CommitDirectory commitDirectory, Map<String, StateNode> stateTree, Set<StateNode> toBeDeleted, Map<StateNode, FileStatusEnum> fileStateMap ) throws IOException {
        for (CommitNode commitNode : commitDirectory.getCommitNodeList()) {
            if (commitNode instanceof CommitDirectory) {
                if (commitNode.getStatusEnum() == FileStatusEnum.IS_DELETED) {
                    toBeDeleted.add(stateTree.get(commitNode.getFullPath()));
                    fileStateMap.put(stateTree.get(commitNode.getFullPath()), FileStatusEnum.IS_DELETED); //

                } else if (commitNode.getStatusEnum() == FileStatusEnum.IS_NEW) {
                    StateDirectory stateDirectory = new StateDirectory(commitNode.getFullPath());
                    StateDirectory parentStateDirectory = (StateDirectory) stateTree.get(commitDirectory.getFullPath());
                    parentStateDirectory.getInternalNodes().add(stateDirectory);
                    stateTree.put(stateDirectory.getPath(),stateDirectory);//
                    fileStateMap.put(stateDirectory, commitNode.getStatusEnum()); //
                }
                addCommitChanges((CommitDirectory) commitNode, stateTree, toBeDeleted, fileStateMap);
            } else {
                CommitFile commitFile = (CommitFile) commitNode;
                if (commitNode.getStatusEnum() == FileStatusEnum.IS_DELETED) {
                    toBeDeleted.add(stateTree.get(commitNode.getFullPath()));
                    fileStateMap.put(stateTree.get(commitNode.getFullPath()), commitNode.getStatusEnum());

                } else if (commitNode.getStatusEnum() == FileStatusEnum.IS_NEW) {
                    StateFile stateFile = new StateFile(commitNode.getFullPath(), commitFile.getNewValuesList());
                    StateDirectory stateDirectory = (StateDirectory) stateTree.get(commitDirectory.getFullPath());
                    stateDirectory.getInternalNodes().add(stateFile);
                    stateTree.put(stateFile.getPath(),stateFile);
                    fileStateMap.put(stateFile, commitNode.getStatusEnum());
                }
                else {
                    updateStateFileWithNewLines(commitFile.getNewValuesList(), commitFile.getLineNumberList(), (StateFile) stateTree.get(commitFile.getFullPath()));
                    fileStateMap.put(stateTree.get(commitNode.getFullPath()),FileStatusEnum.IS_MODIFIED);
                }
            }
        }
    }

    public void reflectStateTreeOnFileSystem(Map<StateNode, FileStatusEnum> fileStateMap ) throws Exception {
        for (Map.Entry<StateNode, FileStatusEnum>entry : fileStateMap.entrySet()) {
            StateNode stateNode = entry.getKey();
            FileStatusEnum fileStatusEnum= entry.getValue();

            try {
                Path path = Paths.get(stateNode.getPath());
                switch (fileStatusEnum) {
                    case IS_NEW:
                        if (stateNode instanceof StateDirectory) {
                            Files.createDirectories(path);
                        } else {
                            Files.createDirectories(path.getParent());
                            Files.write(path, ((StateFile)stateNode).getLines());
                        }
                        break;

                    case IS_MODIFIED:
                        if (Files.isRegularFile(path)) {
                            Files.write(path, ((StateFile)stateNode).getLines());
                        }
                        break;

                    case IS_DELETED:
                        if (Files.exists(path)){
                            deleteFileOrDirectory(path.toString());
                        break;
                        }
                    }

            } catch (Exception exception) {
                throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.FAILED_TO_UPDATE_FILE_SYSTEM, new Object[]{stateNode.getPath()}));
            }
        }
    }

    private void deleteFileOrDirectory(String path){
        File file = new File(path);
        if (file.isFile()){
            file.delete();
            return;
        }
        File[] files = file.listFiles();
        for(File fileInDirectory : files){
            if(fileInDirectory.isDirectory()){
                deleteFileOrDirectory(fileInDirectory.getPath());
            }else{
                fileInDirectory.delete();
            }
        }
        file.delete();
    }

    private void updateStateTreeByRemovingCommits(Map<String, StateNode> stateTree, ArrayList<NinjaNode> removedCommits,  Map<StateNode, FileStatusEnum> fileStateMap ) throws IOException {
        for (NinjaNode ninjaNode : removedCommits) {
            Commit commit = (Commit) ninjaNode;
            CommitDirectory commitDirectory = commit.getCommitTreeRoot();
            Set<StateNode> toBeDeleted = new HashSet<>();
            removeCommitChanges(commitDirectory, stateTree, toBeDeleted, fileStateMap);
            stateTreeRepository.deleteNodeList(toBeDeleted);
        }
    }

    public void removeCommitChanges(CommitDirectory commitDirectory, Map<String, StateNode> stateTree, Set<StateNode> toBeDeleted, Map<StateNode, FileStatusEnum> fileStateMap ) throws IOException {
        for (CommitNode commitNode : commitDirectory.getCommitNodeList()) {
            if (commitNode instanceof CommitDirectory) {
                if (commitNode.getStatusEnum() == FileStatusEnum.IS_DELETED) {
                    StateDirectory stateDirectory = new StateDirectory(commitNode.getFullPath());
                    StateDirectory parentStateDirectory = (StateDirectory) stateTree.get(commitDirectory.getFullPath());
                    stateTree.put(stateDirectory.getPath(),stateDirectory);
                    parentStateDirectory.getInternalNodes().add(stateDirectory);
                    fileStateMap.put(stateDirectory, FileStatusEnum.IS_NEW); //
                } else if (commitNode.getStatusEnum() == FileStatusEnum.IS_NEW) {
                    toBeDeleted.add(stateTree.get(commitNode.getFullPath()));
                    fileStateMap.put(stateTree.get(commitNode.getFullPath()), FileStatusEnum.IS_DELETED);
                }
                removeCommitChanges((CommitDirectory) commitNode, stateTree, toBeDeleted, fileStateMap);
            } else {
                CommitFile commitFile = (CommitFile) commitNode;
                if (commitNode.getStatusEnum() == FileStatusEnum.IS_DELETED) {
                    StateFile stateFile = new StateFile(commitNode.getFullPath(), commitFile.getOldValuesList());
                    StateDirectory stateDirectory = (StateDirectory) stateTree.get(commitDirectory.getFullPath());
                    stateDirectory.getInternalNodes().add(stateFile);
                    stateTree.put(stateFile.getPath(),stateFile);
                    fileStateMap.put(stateFile, FileStatusEnum.IS_NEW);
                } else if (commitNode.getStatusEnum() == FileStatusEnum.IS_NEW) {
                    toBeDeleted.add(stateTree.get(commitNode.getFullPath()));
                    fileStateMap.put(stateTree.get(commitNode.getFullPath()),FileStatusEnum.IS_DELETED);
                } else {
                    updateStateFileWithOldLines(commitFile.getOldValuesList(), commitFile.getLineNumberList(), (StateFile) stateTree.get(commitFile.getFullPath()));
                    fileStateMap.put(stateTree.get(commitNode.getFullPath()),FileStatusEnum.IS_MODIFIED);
                }
            }
        }
    }

    private void updateStateFileWithOldLines(List<String> lines, List<Integer> lineNumberList, StateFile stateFile) {
        for (int i = lineNumberList.size()-1; i >=0 ; i--) {
            int lineNumber = lineNumberList.get(i) - 1;
            if (lineNumber > lines.size()) {
                stateFile.getLines().remove(lineNumber);
            } else {
                stateFile.getLines().set(lineNumber, lines.get(i));
            }
        }
    }

    private void updateStateFileWithNewLines(List<String> newlines, List<Integer> lineNumberList, StateFile stateFile) {
        for (int i = 0; i < lineNumberList.size(); i++) {
            int lineNumber = lineNumberList.get(i) - 1;
            if (lineNumber >= stateFile.getLines().size()) {
                stateFile.getLines().add(newlines.get(i));
            } else {
                stateFile.getLines().set(lineNumber, newlines.get(i));
            }
        }
    }
}