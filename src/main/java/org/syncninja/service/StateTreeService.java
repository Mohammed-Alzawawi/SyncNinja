package org.syncninja.service;

import org.syncninja.model.Branch;
import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.committree.CommitDirectory;
import org.syncninja.model.committree.CommitFile;
import org.syncninja.model.statetree.StateDirectory;
import org.syncninja.model.statetree.StateFile;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.model.statetree.StateNode;
import org.syncninja.model.committree.CommitNode;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.ResourceBundleEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StateTreeService {
    private final StateTreeRepository stateTreeRepository;

    public StateTreeService() {
        stateTreeRepository = new StateTreeRepository();
    }

    public StateFile generateStateFileNode(String path) throws Exception {
        StateFile file = null;
        if (stateTreeRepository.findById(path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.FILE_ALREADY_EXISTS, new Object[]{path}));
        } else {
            file = new StateFile(path);
            stateTreeRepository.save(file);
        }
        StateDirectory parent = (StateDirectory) stateTreeRepository.findById(new File(path).getParent().toString()).orElse(null);
        if (parent == null) {
            parent = new StateDirectory(new File(path).getParent().toString());
        }
        parent.getInternalNodes().add(file);
        stateTreeRepository.save(parent);
        return file;
    }

    public StateDirectory generateStateDirectoryNode(String path) throws Exception {
        StateDirectory stateDirectory = null;
        if (stateTreeRepository.findById(path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.SUB_DIRECTORY_ALREADY_EXISTS, new Object[]{path}));
        } else {
            stateDirectory = new StateDirectory(path);
            stateTreeRepository.save(stateDirectory);
        }
        return stateDirectory;
    }

    public StateNode getStateNode(String path) throws Exception {
        return stateTreeRepository.findById(path).orElseThrow(() ->
                new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.FILE_NOT_FOUND, new Object[]{path})));
    }

    public StateRoot generateStateRootNode(String path, Branch currentBranch) throws Exception {
        StateRoot stateRoot = null;
        if (stateTreeRepository.findById(path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_ALREADY_INITIALIZED, new Object[]{path}));
        } else {
            stateRoot = new StateRoot(path, currentBranch);
            stateTreeRepository.save(stateRoot);
        }
        return stateRoot;
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
    public void updateStateRoot(StateRoot stateRoot, Commit newCommit) {
        stateTreeRepository.updateStateRoot(stateRoot, newCommit);
    }

    public void addChangesToStateTree(CommitNode commitNode, StateNode stateRoot, StateNode parentStateNode) throws Exception {
        List<CommitNode> commitNodeList = new ArrayList<>();
        StateNode currentStateNode;

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

    private StateNode compareAndAddLines(CommitNode commitNode, StateNode currentStateTree) {
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

    private StateDirectory getStateDirectory(String path) {
        return (StateDirectory) stateTreeRepository.findById(path).orElse(
                new StateDirectory(path)
        );
    }
}