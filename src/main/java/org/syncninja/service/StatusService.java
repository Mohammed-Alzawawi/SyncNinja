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
import org.syncninja.util.*;

import java.io.File;
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
                    tracked.add(new CommitFileDTO((CommitFile) commitNode, commitNode.getPath(), Fetcher.getRelativePath(commitNode.getPath())));
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
                StateFile stateFile = (StateFile) stateTreeMap.get(file.getPath());
                CommitFileDTO commitFileDTO = tracked.get(file.getPath());
                if (isModified(stateFile, commitFileDTO, file)) {
                    untracked.add(new StatusFileDTO(stateFile == null, stateFile, file.getPath(), Fetcher.getRelativePath(file.getPath())));
                }
            }
        }
    }

    private void addAllFilesInDirectory(File directory, List<StatusFileDTO> untracked, Map<String, CommitFileDTO> tracked) throws Exception {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                addAllFilesInDirectory(file, untracked, tracked);
            }
            else if (isModified(null, tracked.get(file.getPath()), file)) {
                untracked.add(new StatusFileDTO(true, null, file.getPath(), Fetcher.getRelativePath(file.getPath())));
            }
        }
    }

    public FileTrackingState getState(String path) throws Exception {
        if (stateTreeRepository.findById(path).isEmpty()) {
            return null;
        }
        FileTrackingState fileTrackingState = new FileTrackingState();
        List<CommitFileDTO> trackedFiles = fileTrackingState.getTracked();
        
        //loading the staging area and getting the tracked files
        CommitDirectory stagingArea = getStagingArea(path);
        getTracked(trackedFiles, stagingArea);

        //determining the state of each file (tracked/untracked)
        StateDirectory stateDirectory = (StateDirectory) stateTreeRepository.findById(path).orElse(null);
        Map<String, CommitFileDTO> stagingAreaMap = trackedFiles.stream()
                .collect(Collectors.toMap((commitFileDTO) -> commitFileDTO.getPath(), (commitFileDTO -> commitFileDTO)));

        currentState(new File(path), stateDirectory, fileTrackingState.getUntracked(), stagingAreaMap);
        return fileTrackingState;
    }


    public CommitDirectory getStagingArea(String path) throws Exception {
        StateRoot stateRoot = (StateRoot) stateTreeRepository.findById(path).orElse(null);
        NinjaNode currentCommit = stateRoot.getCurrentCommit();
        if (currentCommit == null) {
            currentCommit = stateRoot.getCurrentBranch();
        }
        if (currentCommit.getNextCommit() == null) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.STAGE_AREA_IS_EMPTY));
        }
        return currentCommit.getNextCommit().getCommitTree();
    }
    //checking the state of the file
    public boolean isModified(StateFile stateFile, CommitFileDTO commitFileDTO, File file) throws Exception {
        if(stateFile != null && stateFile.getLastModified() == file.lastModified()){
            return false;
        }
        LinesContainer linesContainer = CompareFileUtil.compareFiles(file.getPath(), new StatusFileDTO(stateFile==null, stateFile, file.getPath(), Fetcher.getRelativePath(file.getPath())));
        if (commitFileDTO == null ) {
            return !linesContainer.getLineNumbers().isEmpty();
        }
        if(!commitFileDTO.getCommitFile().getNewValuesList().equals(linesContainer.getNewLines())){
            return true;
        }
        return false;
    }
}