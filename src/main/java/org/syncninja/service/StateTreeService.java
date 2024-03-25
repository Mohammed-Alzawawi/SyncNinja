package org.syncninja.service;

import org.syncninja.dto.StatusFileDTO;
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
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.FileTrackingState;
import org.syncninja.util.Regex;
import org.syncninja.util.ResourceBundleEnum;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StateTreeService {
    private final StateTreeRepository stateTreeRepository;
    private final StatusService statusService;

    public StateTreeService() {
        stateTreeRepository = new StateTreeRepository();
        statusService = new StatusService();
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
        if (currentNode == null) {
            currentNode = stateRoot.getCurrentBranch();
        }
        Commit commit = currentNode.getNextCommit();
        if (commit == null) {
            throw new Exception("Commit tree is no staging area init");
        }
        return commit.getCommitTreeRoot();
    }

    public void updateStateRoot(StateRoot stateRoot, Commit newCommit) {
        stateTreeRepository.updateStateRoot(stateRoot, newCommit);
    }

    public void addChangesToStateTree(CommitNode commitNode, StateNode parentStateNode) throws Exception {
        List<CommitNode> commitNodeList = new ArrayList<>();
        StateNode currentStateNode;

        if (commitNode instanceof CommitDirectory) {
            currentStateNode = getStateDirectory(commitNode.getPath());
            commitNodeList = ((CommitDirectory) commitNode).getCommitNodeList();
        } else {
            currentStateNode = compareAndAddLines(commitNode, getStateFile(commitNode.getPath()));
        }

        if (parentStateNode != null && !((StateDirectory) parentStateNode).getInternalNodes().contains(currentStateNode)) {
            ((StateDirectory) parentStateNode).addFile(currentStateNode);
        }

        for (CommitNode childCommitNode : commitNodeList) {
            addChangesToStateTree(childCommitNode, currentStateNode);
        }

        // save the root
        if (parentStateNode == null) {
            stateTreeRepository.save(currentStateNode);
        }
    }

    private StateNode compareAndAddLines(CommitNode commitNode, StateNode currentStateTree) {
        CommitFile commitFile = ((CommitFile) commitNode);
        List<String> stateFileLines = ((StateFile) currentStateTree).getLines();

        int commitFileLineIndex = 0;
        while (commitFileLineIndex < commitFile.getLineNumberList().size()) {
            String newLine = commitFile.getNewValuesList().get(commitFileLineIndex);
            int newLineNumber = commitFile.getLineNumberList().get(commitFileLineIndex) - 1;

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

    private StateDirectory getStateDirectory(String path) throws IOException {
        return (StateDirectory) stateTreeRepository.findById(path).orElse(
                new StateDirectory(path)
        );
    }

    public void restore(List<String> pathList, String mainDirectoryPath) throws Exception {
        StateRoot stateRoot = getStateRoot(mainDirectoryPath);

        Regex regexBuilder = new Regex();
        for (String path : pathList) {
            regexBuilder.addFilePath(path);
        }
        String regex = regexBuilder.buildRegex();

        FileTrackingState fileTrackingState = statusService.getState(mainDirectoryPath);
        if (fileTrackingState == null) {
            throw new IllegalStateException("Failed to retrieve file tracking state for directory: " + mainDirectoryPath);
        }
        List<StatusFileDTO> untrackedFiles = fileTrackingState.getUntracked();
        List<String> untrackedPaths = new ArrayList<>();

        for(StatusFileDTO statusFileDTO : untrackedFiles){
            untrackedPaths.add(statusFileDTO.getPath());
        }

        restoreFiles(stateRoot, regex, untrackedPaths);
    }

    private void restoreFiles(StateNode currentStateTree, String regex, List<String> untrackedPaths) throws IOException {
        List<StateNode> stateNodeList = new ArrayList<>();
        String currentStateTreePath = currentStateTree.getPath();

        if(currentStateTree instanceof StateDirectory){
            stateNodeList = ((StateDirectory)currentStateTree).getInternalNodes();
        } else if(currentStateTreePath.matches(regex) && untrackedPaths.contains(currentStateTreePath)){
            restoreOldLines(currentStateTreePath, currentStateTree);
        }

        for(StateNode stateNode : stateNodeList){
            restoreFiles(stateNode, regex, untrackedPaths);
        }
    }

    private void restoreOldLines(String path, StateNode stateNode) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (String line : ((StateFile)stateNode).getLines()) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}