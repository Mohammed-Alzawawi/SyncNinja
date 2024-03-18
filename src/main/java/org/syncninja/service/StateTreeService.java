package org.syncninja.service;

import org.syncninja.model.Branch;
import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.StateTree.StateDirectory;
import org.syncninja.model.StateTree.StateFile;
import org.syncninja.model.StateTree.StateRoot;
import org.syncninja.model.StateTree.StateTree;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitFile;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.ResourceBundleEnum;

import java.util.ArrayList;
import java.util.List;

public class StateTreeService {
    private final StateTreeRepository stateTreeRepository;

    public StateTreeService() {
        stateTreeRepository = new StateTreeRepository();
    }

    public StateTree getStateNode(String path) throws Exception {
        return stateTreeRepository.findById(path).orElseThrow(() ->
                new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.FILE_NOT_FOUND, new Object[]{path})));
    }

    public void generateStateRootNode(String path, Branch currentBranch) throws Exception {
        if (stateTreeRepository.findById(path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_ALREADY_INITIALIZED, new Object[]{path}));
        } else {
            StateRoot stateRoot = new StateRoot(path, currentBranch);
            stateTreeRepository.save(stateRoot);
        }
    }

    public StateRoot getStateRoot(String path) throws Exception {
        return (StateRoot) stateTreeRepository.findById(path).orElseThrow(
                () -> new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path})));
    }

    public CommitNode getStagingArea(String path) throws Exception {
        StateRoot stateRoot = getStateRoot(path);
        NinjaNode currentNode = stateRoot.getCurrentCommit();
        if(currentNode == null){
            currentNode = stateRoot.getCurrentBranch();
        }
        Commit commit = currentNode.getNextCommit();
        if(commit == null) {
            throw new Exception("Commit tree is no staging area init");
        }
        return commit.getCommitTreeRoot();
    }
    public void updateStateRootCurrentCommit(StateRoot stateRoot, Commit newCommit) {
        stateTreeRepository.updateStateRoot(stateRoot, newCommit);
    }

    public void addChangesToStateTree(CommitNode commitNode, StateTree stateRoot, StateTree parentStateNode) throws Exception {
        List<CommitNode> commitNodeList = new ArrayList<>();
        StateTree currentStateNode;

        if(commitNode instanceof CommitDirectory){
            currentStateNode = getStateDirectory(commitNode.getPath());
            commitNodeList = ((CommitDirectory) commitNode).getCommitNodeList();
        } else {
            currentStateNode = compareAndAddLines(commitNode, getStateFile(commitNode.getPath()));
        }

        if(parentStateNode != null &&!((StateDirectory)parentStateNode).getInternalNodes().contains(currentStateNode)){
            ((StateDirectory)parentStateNode).addFile(currentStateNode);
        }

        for (CommitNode childCommitNode : commitNodeList) {
            addChangesToStateTree(childCommitNode, stateRoot, currentStateNode);
        }

        // save the root
        if (parentStateNode == null){
            stateTreeRepository.save(currentStateNode);
        }
    }

    private StateTree compareAndAddLines(CommitNode commitNode, StateTree currentStateTree) {
        CommitFile commitFile = ((CommitFile)commitNode);
        List<String> stateFileLines = ((StateFile)currentStateTree).getLines();

        int commitFileLineIndex = 0;
        while(commitFileLineIndex < commitFile.getLineNumberList().size()){
            String newLine = commitFile.getNewValuesList().get(commitFileLineIndex);
            int newLineNumber = commitFile.getLineNumberList().get(commitFileLineIndex)-1;

            if (newLineNumber < stateFileLines.size()) {
                ((StateFile) currentStateTree).getLines().remove(newLineNumber);
            }

            ((StateFile) currentStateTree).getLines().add(newLineNumber, newLine);
            commitFileLineIndex++;
        }
        return currentStateTree;
    }

    private StateFile getStateFile(String path) throws Exception {
        return (StateFile) stateTreeRepository.findById(path).orElse(
                new StateFile(path)
        );
    }

    private StateDirectory getStateDirectory(String path) throws Exception {
        return (StateDirectory) stateTreeRepository.findById(path).orElse(
                new StateDirectory(path)
        );
    }
}