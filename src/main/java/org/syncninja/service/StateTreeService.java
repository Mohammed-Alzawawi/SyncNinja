package org.syncninja.service;

import org.syncninja.dto.FileStatusEnum;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StateTreeService {
    private final StateTreeRepository stateTreeRepository;
    private final StatusService statusService;

    public StateTreeService() {
        stateTreeRepository = new StateTreeRepository();
        statusService = new StatusService();
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

    public void addChangesToStateTree(CommitDirectory commitDirectory, StateDirectory stateDirectory) throws IOException {
        Map<String, StateNode> stateTreeMap = stateDirectory.getInternalNodes().stream()
                .collect(Collectors.toMap(StateNode::getPath, (stateTree -> stateTree)));
        List<CommitNode> commitNodeList = commitDirectory.getCommitNodeList();

        for (CommitNode commitNode : commitNodeList) {
            StateNode currentStateNode = stateTreeMap.get(commitNode.getFullPath());

            // delete the deleted files from state tree
            if (commitNode.getStatusEnum() == FileStatusEnum.IS_DELETED) {
                stateDirectory.getInternalNodes().remove(currentStateNode);
                stateTreeRepository.delete(currentStateNode);
                continue;
            }
            if (commitNode instanceof CommitDirectory) {
                if (currentStateNode == null) {
                    currentStateNode = new StateDirectory(commitNode.getFullPath());
                    stateDirectory.getInternalNodes().add(currentStateNode);
                }
                addChangesToStateTree((CommitDirectory) commitNode, (StateDirectory) currentStateNode);
            } else {
                if (currentStateNode == null) {
                    currentStateNode = new StateFile(commitNode.getFullPath());
                    stateDirectory.getInternalNodes().add(currentStateNode);
                }
                ((StateFile) currentStateNode).setLines(compareAndAddLines(commitNode, currentStateNode));
            }
        }
        if (stateDirectory instanceof StateRoot) {
            stateTreeRepository.save(stateDirectory);
        }
    }

    private List<String> compareAndAddLines(CommitNode commitNode, StateNode currentStateTree) {
        CommitFile commitFile = ((CommitFile) commitNode);
        List<String> stateFileLines = ((StateFile) currentStateTree).getLines();

        int commitFileLineIndex = 0;
        while (commitFileLineIndex < commitFile.getLineNumberList().size()) {
            String newLine = commitFile.getNewValuesList().get(commitFileLineIndex);
            int newLineNumber = commitFile.getLineNumberList().get(commitFileLineIndex) - 1;

            if (newLineNumber < stateFileLines.size()) {
                stateFileLines.remove(newLineNumber);
            }

            while (newLineNumber > stateFileLines.size()) {
                stateFileLines.add("\n");
            }

            stateFileLines.add(newLineNumber, newLine);
            commitFileLineIndex++;
        }

        return stateFileLines;
    }

    public void restore(List<String> pathList, String mainDirectoryPath) throws Exception {
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

        for (StatusFileDTO statusFileDTO : untrackedFiles) {
            if (statusFileDTO.getPath().matches(regex) && statusFileDTO.getStateFile() != null) {
                restoreOldLines(statusFileDTO.getPath(), statusFileDTO.getStateFile());
            }
        }
    }

    private void restoreOldLines(String path, StateNode stateNode) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            List<String> lines = ((StateFile) stateNode).getLines();
            for (int index = 0; index < lines.size(); index++) {
                writer.write(lines.get(index));
                if (index != lines.size() - 1) {
                    writer.newLine();
                }
            }
        }
    }

    public Map<String, StateNode> getStateTree(String path) throws Exception {
        StateRoot stateRoot = getStateRoot(path);
        Map<String, StateNode> stateTree = new HashMap<>();
        updateStateTreeMap(stateTree, stateRoot);
        return stateTree;
    }

    public void updateStateTreeMap(Map<String, StateNode> stateNodeMap, StateDirectory stateDirectory) {
        for (StateNode stateNode : stateDirectory.getInternalNodes()) {
            if (stateNode instanceof StateDirectory) {
                stateNodeMap.put(stateNode.getPath(), stateNode);
                updateStateTreeMap(stateNodeMap, (StateDirectory) stateNode);
            } else {
                stateNodeMap.put(stateNode.getPath(), stateNode);
            }
        }
    }
}