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

    public void updateStateRoot(StateRoot stateRoot, NinjaNode ninjaNode) {
        stateTreeRepository.updateStateRoot(stateRoot, ninjaNode);
    }

    public void addChangesToStateTree(CommitNode commitNode, StateNode parentStateNode) throws Exception {
        List<CommitNode> commitNodeList = new ArrayList<>();
        StateNode currentStateNode;

        if (commitNode instanceof CommitDirectory) {
            currentStateNode = getStateDirectory(commitNode.getFullPath());
            commitNodeList = ((CommitDirectory) commitNode).getCommitNodeList();
        } else {
            currentStateNode = compareAndAddLines(commitNode, getStateFile(commitNode.getFullPath()));
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

        // building regex
        Regex regexBuilder = new Regex();
        for (String path : pathList) {
            regexBuilder.addFilePath(path);
        }
        String regex = regexBuilder.buildRegex();

        // looping through the untracked
        FileTrackingState fileTrackingState = statusService.getState(mainDirectoryPath);
        if (fileTrackingState == null) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{mainDirectoryPath}));
        }
        List<StatusFileDTO> untrackedFiles = fileTrackingState.getUntracked();

        for(StatusFileDTO statusFileDTO : untrackedFiles){
            if(statusFileDTO.getPath().matches(regex) && statusFileDTO.getStateFile() != null){
                restoreOldLines(statusFileDTO.getPath(), statusFileDTO.getStateFile());
            }
        }
    }

    private void restoreOldLines(String path, StateNode stateNode) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            List<String> lines = ((StateFile) stateNode).getLines();
            for(int index = 0; index < lines.size(); index++) {
                writer.write(lines.get(index));
                if(index != lines.size() - 1) {
                    writer.newLine();
                }
            }
        }
    }
}