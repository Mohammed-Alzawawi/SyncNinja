package org.syncninja.service;

import org.syncninja.dto.CommitFileDTO;
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
import org.syncninja.util.Fetcher;
import org.syncninja.util.FileTrackingState;
import org.syncninja.util.Regex;
import org.syncninja.util.ResourceBundleEnum;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public void generateStateRootNode(String path, Branch currentBranch) throws Exception {
        if (stateTreeRepository.findById(path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_ALREADY_INITIALIZED, new Object[]{path}));
        } else {
            StateRoot stateRoot = new StateRoot(path, currentBranch);
            stateTreeRepository.save(stateRoot);
        }
    }

    public StateRoot getStateRoot(String path) throws Exception {
        return (StateRoot) stateTreeRepository.findById(path).orElseThrow(() -> new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path})));
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
        Map<String, StateNode> stateTreeMap = stateDirectory.getInternalNodes().stream().collect(Collectors.toMap(StateNode::getPath, (stateTree -> stateTree)));
        List<CommitNode> commitNodeList = commitDirectory.getCommitNodeList();

        for (CommitNode commitNode : commitNodeList) {
            StateNode currentStateNode = stateTreeMap.get(commitNode.getFullPath());
            if (commitNode.getStatusEnum() == FileStatusEnum.IS_DELETED) {
                stateDirectory.getInternalNodes().remove(currentStateNode);
                stateTreeRepository.delete(currentStateNode);
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
                ((StateFile) currentStateNode).setLines(compareAndAddLines((CommitFile) commitNode, currentStateNode));
            }
        }
        if (stateDirectory instanceof StateRoot) {
            stateTreeRepository.save(stateDirectory);
        }
    }

    public StateNode findStateNodeByPath(String path) {
         return stateTreeRepository.findById(path).get();
    }

    private List<String> compareAndAddLines(CommitFile commitFile, StateNode currentStateTree) {
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
            regexBuilder.addFilePath(Fetcher.getPathForQuery(path));
        }
        String regex = regexBuilder.buildRegex();

        FileTrackingState fileTrackingState = statusService.getState(mainDirectoryPath);
        if (fileTrackingState == null) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{mainDirectoryPath}));
        }
        List<StatusFileDTO> untrackedFiles = fileTrackingState.getUntracked();
        List<CommitFileDTO> trackedFiles = fileTrackingState.getTracked();
        Map<String, CommitFileDTO> trackedFilesMap = trackedFiles.stream().collect(Collectors.toMap(CommitFileDTO::getPath, (commitFileDTO -> commitFileDTO)));

        // looping through the untracked
        restoreFiles(untrackedFiles, trackedFilesMap, regex);
    }

    private void restoreFiles(List<StatusFileDTO> untrackedFiles, Map<String, CommitFileDTO> trackedFilesMap, String regex) throws IOException {
        for (StatusFileDTO statusFileDTO : untrackedFiles) {
            CommitFileDTO commitFileDto = trackedFilesMap.get(statusFileDTO.getPath());

            if (statusFileDTO.getRelativePath().matches(regex)) {
                // if the file has a state node
                if (statusFileDTO.getStateFile() != null) {
                    restoreOldLines(statusFileDTO.getPath(), statusFileDTO.getStateFile());

                    if (commitFileDto != null) {
                        if (commitFileDto.getCommitFile().getStatusEnum() != FileStatusEnum.IS_DELETED) {
                            restoreOldLinesFromStagingArea(statusFileDTO.getPath(), commitFileDto, statusFileDTO.getStateFile());
                        } else {
                            Path path = Paths.get(statusFileDTO.getPath());
                            Files.delete(path);
                        }
                    }
                }

                // this checks if the file is new
                else {
                    if (commitFileDto != null) {
                        if (commitFileDto.getCommitFile().getStatusEnum() != FileStatusEnum.IS_DELETED) {
                            restoreNewFileFromStagingArea(statusFileDTO.getPath(), commitFileDto);
                        } else {
                            Path path = Paths.get(statusFileDTO.getPath());
                            Files.delete(path);
                        }
                    } else {
                        Path path = Paths.get(statusFileDTO.getPath());
                        Files.delete(path);
                    }
                }
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

    private void restoreOldLinesFromStagingArea(String path, CommitFileDTO commitFileDTO, StateFile stateFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            List<String> lines = compareAndAddLines(commitFileDTO.getCommitFile(), stateFile);
            for (int index = 0; index < lines.size(); index++) {
                writer.write(lines.get(index));
                if (index != lines.size() - 1) {
                    writer.newLine();
                }
            }
        }
    }

    private void restoreNewFileFromStagingArea(String path, CommitFileDTO commitFileDTO) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            List<String> lines = compareAndAddLines(commitFileDTO.getCommitFile(), new StateFile());
            for (int index = 0; index < lines.size(); index++) {
                writer.write(lines.get(index));
                if (index != lines.size() - 1) {
                    writer.newLine();
                }
            }
        }
    }

    public Map<String, StateNode> getStateTree(StateRoot stateRoot) {
        Map<String, StateNode> stateTree = new HashMap<>();
        stateTree.put(stateRoot.getPath(), stateRoot);
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
