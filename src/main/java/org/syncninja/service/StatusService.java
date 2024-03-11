package org.syncninja.service;


import org.syncninja.dto.CommitFileDTO;
import org.syncninja.dto.StatusFileDTO;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.StateTree.StateDirectory;
import org.syncninja.model.StateTree.StateFile;
import org.syncninja.model.StateTree.StateRoot;
import org.syncninja.model.StateTree.StateTree;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitFile;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.CompareFileUtil;
import org.syncninja.util.Fetcher;
import org.syncninja.util.FileTrackingState;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatusService {
    private final StateTreeRepository stateTreeRepository;
    private final CommitNodeRepository commitNodeRepository;
    public StatusService() {
        this.stateTreeRepository = new StateTreeRepository();
        this.commitNodeRepository = new CommitNodeRepository();
    }

    public void getTracked(List<CommitFileDTO> tracked, CommitDirectory commitDirectory) {
        if (commitDirectory != null) {
            List<CommitNode> trackedList = commitDirectory.getCommitNodeList();
            for (CommitNode commitNode : trackedList) {
                if (commitNode instanceof CommitDirectory) {
                    getTracked(tracked, (CommitDirectory) commitNode);
                } else {
                    tracked.add(new CommitFileDTO((CommitFile) commitNode, commitNode.getPath()));
                }
            }
        }
    }

    public void currentState(File directory, StateDirectory stateDirectory, List<StatusFileDTO> untracked, Map<String, CommitFileDTO> tracked) throws Exception {
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
                    if(CompareFileUtil.isModified(file.getPath() , tracked.get(file.getPath()))){
                        untracked.add(new StatusFileDTO(true , null , file.getPath()));
                    }
                    continue;
                }
                StateFile stateFile = (StateFile) stateTreeMap.get(file.getPath());
                if (stateFile == null) {
                    untracked.add(new StatusFileDTO(true, null, file.getPath()));
                } else if(stateFile.getLastModified() != file.lastModified() && CompareFileUtil.isModified(file.getPath(), new StatusFileDTO(false , stateFile , stateFile.getPath()))) {
                    untracked.add(new StatusFileDTO(false, stateFile, file.getPath()));
                }
            }
        }
    }

    private void addAllFilesInDirectory(File directory, List<StatusFileDTO> untracked, Map<String, CommitFileDTO> tracked) throws Exception {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                addAllFilesInDirectory(file, untracked, tracked);
            } else if (tracked.get(file.getPath()) == null) {
                untracked.add(new StatusFileDTO(true, null, file.getPath()));
            }
            else if(CompareFileUtil.isModified(file.getPath() , tracked.get(file.getPath()))){
                untracked.add(new StatusFileDTO(true, null , file.getPath()));
            }
        }
    }

    public FileTrackingState getState(String path) throws Exception {
        if (stateTreeRepository.findById(path).isEmpty()) {
            return null;
        }
        FileTrackingState fileTrackingState = new FileTrackingState();
        List<CommitFileDTO> trackedFiles = fileTrackingState.getTracked();
        CommitDirectory stagingArea = getStagingArea(path);
        getTracked(trackedFiles, stagingArea);
        StateDirectory stateDirectory = (StateDirectory) stateTreeRepository.findById(path).orElse(null);
        Map<String, CommitFileDTO> stagingAreaMap = trackedFiles.stream()
                .collect(Collectors.toMap((commitFileDTO) -> commitFileDTO.getPath(), (commitFileDTO -> commitFileDTO)));

        currentState(new File(path), stateDirectory, fileTrackingState.getUntracked(), stagingAreaMap);
        return fileTrackingState;
    }


    public CommitDirectory getStagingArea(String path) throws Exception {
        StateRoot stateRoot = (StateRoot) stateTreeRepository.findById(path).orElse(null);
        NinjaNode currentCommit = stateRoot.getCurrentCommit();
        if(currentCommit==null){
            currentCommit = stateRoot.getCurrentBranch();
        }
        return currentCommit.getNextCommit().getCommitTree();
    }
}