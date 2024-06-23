package org.syncninja.service;

import org.syncninja.dto.CommitFileDTO;
import org.syncninja.dto.FileStatusEnum;
import org.syncninja.dto.StatusFileDTO;
import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.committree.CommitDirectory;
import org.syncninja.model.committree.CommitFile;
import org.syncninja.model.committree.CommitNode;
import org.syncninja.model.committree.mergetree.MergeCommit;
import org.syncninja.model.statetree.StateDirectory;
import org.syncninja.model.statetree.StateFile;
import org.syncninja.model.statetree.StateNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatusService {
    private final StateTreeRepository stateTreeRepository;

    public StatusService() {
        this.stateTreeRepository = new StateTreeRepository();
    }

    public void getTracked(List<CommitFileDTO> tracked, CommitDirectory commitDirectory, List<StatusFileDTO> untracked) {
        if (commitDirectory != null) {
            List<CommitNode> trackedList = commitDirectory.getCommitNodeList();
            for (CommitNode commitNode : trackedList) {
                if (commitNode instanceof CommitDirectory) {
                    getTracked(tracked, (CommitDirectory) commitNode, untracked);
                } else {
                    if (commitNode.getStatusEnum() == FileStatusEnum.IS_NEW && !(new File(commitNode.getFullPath()).exists())) {
                        untracked.add(new StatusFileDTO(FileStatusEnum.IS_DELETED, null, commitNode.getFullPath(), commitNode.getPath()));
                    }
                    tracked.add(new CommitFileDTO((CommitFile) commitNode, commitNode.getFullPath(), Fetcher.getRelativePath(commitNode.getFullPath())));
                }
            }
        }
    }

    private boolean isDeleted(Map<String, CommitFileDTO> tracked, StateNode stateNode) {
        CommitFileDTO commitFileDTO = tracked.get(stateNode.getPath());
        if (commitFileDTO == null) {
            return false;
        }
        return commitFileDTO.getCommitFile().getStatusEnum() == FileStatusEnum.IS_DELETED;
    }

    public void getDeleted(List<StatusFileDTO> untracked, StateDirectory rootDirectory, File[] filesList, Map<String, FileStatusEnum> directoriesState, Map<String, CommitFileDTO> tracked) {
        if (rootDirectory != null) {
            List<StateNode> stateTree = rootDirectory.getInternalNodes();
            List<File> files = List.of(filesList);
            for (StateNode stateNode : stateTree) {
                if (stateNode instanceof StateDirectory && !files.contains(new File(stateNode.getPath()))) {
                    //to be checked later to see if it causes problems
                    directoriesState.put(stateNode.getPath(), FileStatusEnum.IS_DELETED);
                    getDeleted(untracked, (StateDirectory) stateNode, filesList, directoriesState, tracked);
                } else if (!files.contains(new File(stateNode.getPath())) && !isDeleted(tracked, stateNode)) {
                    untracked.add(new StatusFileDTO(FileStatusEnum.IS_DELETED, (StateFile) stateNode, stateNode.getPath(), Fetcher.getRelativePath(stateNode.getPath())));
                }
            }
        }
    }

    public void currentState(StateDirectory stateDirectory, List<StatusFileDTO> untracked, Map<String, CommitFileDTO> tracked, File[] filesList, Map<String, FileStatusEnum> directoriesState) throws Exception {
        Map<String, StateNode> stateTreeMap = stateDirectory.getInternalNodes().stream()
                .collect(Collectors.toMap((stateTree) -> stateTree.getPath(), (stateTree -> stateTree)));
        getDeleted(untracked, stateDirectory, filesList, directoriesState, tracked);
        for (File file : filesList) {
            if (file.isDirectory()) {
                StateDirectory stateDirectoryChild = (StateDirectory) stateTreeMap.get(file.getPath());

                // creates a path object, so we can get the last access time for this directory
                Path path = Paths.get(file.toString());
                BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                if (stateDirectoryChild == null) {
                    // the directory is new add everything inside it
                    directoriesState.put(file.getPath(), FileStatusEnum.IS_NEW);
                    addAllFilesInDirectory(file, untracked, tracked, directoriesState);
                } else if (stateDirectoryChild.getLastAccessed() != attrs.lastAccessTime().toMillis()) {
                    File[] filesInDirectory = file.listFiles();
                    currentState(stateDirectoryChild, untracked, tracked, filesInDirectory, directoriesState);
                }
            } else {
                StateFile stateFile = (StateFile) stateTreeMap.get(file.getPath());
                CommitFileDTO commitFileDTO = tracked.get(file.getPath());
                if (isModified(stateFile, commitFileDTO, file)) {
                    FileStatusEnum fileStatusEnum = FileStatusEnum.IS_MODIFIED;
                    if (stateFile == null || (commitFileDTO != null && commitFileDTO.getCommitFile() != null && commitFileDTO.getCommitFile().getStatusEnum() == FileStatusEnum.IS_DELETED)) {
                        fileStatusEnum = FileStatusEnum.IS_NEW;
                    }
                    untracked.add(new StatusFileDTO(fileStatusEnum, stateFile, file.getPath(), Fetcher.getRelativePath(file.getPath())));
                }
            }
        }
    }

    private void addAllFilesInDirectory(File directory, List<StatusFileDTO> untracked, Map<String, CommitFileDTO> tracked, Map<String, FileStatusEnum> directoriesState) throws Exception {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                directoriesState.put(file.getPath(), FileStatusEnum.IS_NEW);
                addAllFilesInDirectory(file, untracked, tracked, directoriesState);
            } else if (isModified(null, tracked.get(file.getPath()), file)) {
                untracked.add(new StatusFileDTO(FileStatusEnum.IS_NEW, null, file.getPath(), Fetcher.getRelativePath(file.getPath())));
            }
        }
    }

    public FileTrackingState getState(String path) throws Exception {
        if (stateTreeRepository.findById(path).isEmpty()) {
            return null;
        }
        FileTrackingState fileTrackingState = new FileTrackingState();
        List<CommitFileDTO> trackedFiles = fileTrackingState.getTracked();
        Map<String, FileStatusEnum> directoriesState = fileTrackingState.getDirectoriesState();

        //loading the staging area and getting the tracked files
        CommitDirectory stagingArea = getStagingArea(path);
        getTracked(trackedFiles, stagingArea, fileTrackingState.getUntracked());

        //determining the state of each file (tracked/untracked)
        StateDirectory stateDirectory = (StateDirectory) stateTreeRepository.findById(path).orElse(null);
        Map<String, CommitFileDTO> stagingAreaMap = trackedFiles.stream()
                .collect(Collectors.toMap((commitFileDTO) -> commitFileDTO.getPath(), (commitFileDTO -> commitFileDTO)));
        File mainFileDirectory = new File(path);
        File[] filesList = mainFileDirectory.listFiles();
        currentState(stateDirectory, fileTrackingState.getUntracked(), stagingAreaMap, filesList, directoriesState);
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
        return currentCommit.getNextCommit().getCommitTreeRoot();
    }

    //checking the state of the file
    public boolean isModified(StateFile stateFile, CommitFileDTO commitFileDTO, File file) throws Exception {
        if (commitFileDTO != null && commitFileDTO.getCommitFile().getStatusEnum() == FileStatusEnum.IS_DELETED) {
            return true;
        }
        if (stateFile != null && stateFile.getLastModified() == file.lastModified()) {
            return false;
        }
        LinesContainer linesContainer = CompareFileUtil.compareFiles(file.getPath(), new StatusFileDTO(stateFile == null ? FileStatusEnum.IS_NEW : FileStatusEnum.IS_MODIFIED, stateFile, file.getPath(), Fetcher.getRelativePath(file.getPath())));
        if (commitFileDTO == null) {
            return !linesContainer.getLineNumbers().isEmpty();
        }
        return !commitFileDTO.getCommitFile().getNewValuesList().equals(linesContainer.getNewLines());
    }

    public boolean hasConflict(StateRoot stateRoot){
        Commit nextCommit = stateRoot.getCurrentNinjaNode().getNextCommit();
        return (nextCommit instanceof MergeCommit && !nextCommit.isCommitted());
    }
}
