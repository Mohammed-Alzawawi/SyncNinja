package org.syncninja.service;


import org.syncninja.dto.CommitFileDTO;
import org.syncninja.dto.StatusFileDTO;
import org.syncninja.model.StateTree.StateDirectory;
import org.syncninja.model.StateTree.StateFile;
import org.syncninja.model.StateTree.StateTree;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitFile;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.FileTrackingState;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatusService {
    private final StateTreeRepository stateTreeRepository;
    private final CommitNodeRepository commitNodeRepository;

    public StatusService() {
        stateTreeRepository = new StateTreeRepository();
        commitNodeRepository = new CommitNodeRepository();
    }

    public void getTracked(List<CommitFileDTO> tracked, CommitDirectory commitDirectory) {
        if (commitDirectory != null) {
            List<CommitNode> trackedList = commitDirectory.getCommitNodeList();
            for (CommitNode commitNode : trackedList) {
                if (commitNode.isDirectory()) {
                    getTracked(tracked, (CommitDirectory) commitNode);
                } else {
                    tracked.add(new CommitFileDTO((CommitFile) commitNode, commitNode.getPath()));
                }
            }
        }
    }

    public void currentState(File directory, StateDirectory stateDirectory, List<StatusFileDTO> untracked, Map<String, CommitFileDTO> tracked) {
        File[] filesList = directory.listFiles();
        Map<String, StateTree> stateTreeMap = stateDirectory.getInternalNodes().stream()
                .collect(Collectors.toMap((stateTree) -> stateTree.getPath(), (stateTree -> stateTree)));
        for (File file : filesList) {
            if (file.isDirectory()) {
                StateDirectory stateDirectoryChild = (StateDirectory) stateTreeMap.get(file.getPath());
                if (stateDirectoryChild == null) {
                    addAllFilesInDirectory(file, untracked, tracked);
                } else if (stateDirectoryChild.getLastModified() != file.lastModified()) {
                    currentState(file, stateDirectoryChild, untracked, tracked);
                }
            } else {
                if (tracked.get(file.getPath()) != null) {
                    continue;
                }
                StateFile stateFile = (StateFile) stateTreeMap.get(file.getPath());
                if (stateFile == null) {
                    untracked.add(new StatusFileDTO(true, null, file.getPath()));
                } else if(stateFile.getLastModified()!=file.lastModified()) {
                    untracked.add(new StatusFileDTO(false, stateFile, file.getPath()));
                }
            }
        }
    }

    private void addAllFilesInDirectory(File directory, List<StatusFileDTO> untracked, Map<String, CommitFileDTO> tracked) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                addAllFilesInDirectory(file, untracked, tracked);
            } else if (tracked.get(file.getPath()) == null) {
                untracked.add(new StatusFileDTO(true, null, file.getPath()));
            }
        }
    }

    public FileTrackingState getState(String path) throws Exception {
        if (stateTreeRepository.findById(path).isEmpty()) {
            return null;
        }
        FileTrackingState fileTrackingState = new FileTrackingState();
        List<CommitFileDTO> trackedFiles = fileTrackingState.getTracked();
        CommitDirectory stagingArea = (CommitDirectory) commitNodeRepository.findByPath(path).orElse(null);
        getTracked(trackedFiles, stagingArea);
        StateDirectory stateDirectory = (StateDirectory) stateTreeRepository.findById(path).orElse(null);
        Map<String, CommitFileDTO> stagingAreaMap = trackedFiles.stream()
                .collect(Collectors.toMap((commitFileDTO) -> commitFileDTO.getPath(), (commitFileDTO -> commitFileDTO)));

        currentState(new File(path), stateDirectory, fileTrackingState.getUntracked(), stagingAreaMap);
        return fileTrackingState;
    }
}