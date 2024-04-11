package org.syncninja.service;

import org.syncninja.dto.FileStatusEnum;
import org.syncninja.dto.StatusFileDTO;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.committree.CommitDirectory;
import org.syncninja.model.committree.CommitFile;
import org.syncninja.model.committree.CommitNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommitTreeService {
    private final StatusService statusService;
    private final CommitNodeRepository commitNodeRepository;
    private final CommitService commitService;
    private final StateTreeService stateTreeService;


    public CommitTreeService() {
        this.stateTreeService = new StateTreeService();
        this.statusService = new StatusService();
        this.commitNodeRepository = new CommitNodeRepository();
        this.commitService = new CommitService();
    }

    public void addFileToCommitTree(String mainDirectoryPath, List<String> listOfFilesToBeAdded) throws Exception {
        FileTrackingState fileTrackingState = statusService.getState(mainDirectoryPath);
        if (fileTrackingState == null) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{mainDirectoryPath}));
        }

        if (fileTrackingState.getUntracked() == null || fileTrackingState.getUntracked().isEmpty()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.NO_CHANGES_TO_BE_ADDED));
        }

        List<StatusFileDTO> untrackedFiles = fileTrackingState.getUntracked();
        Map<String, FileStatusEnum> directoriesState = fileTrackingState.getDirectoriesState();
        addFilesToCommitTree(untrackedFiles, mainDirectoryPath, listOfFilesToBeAdded, directoriesState);
    }

    private void addFilesToCommitTree(List<StatusFileDTO> statusFileDTOs, String mainDirectoryPath, List<String> listOfFilesToBeAdded,
                                      Map<String, FileStatusEnum> directoriesState) throws Exception {
        CommitDirectory root = createAndGetCommitTreeRoot(mainDirectoryPath);

        // building regex for add command
        Regex regexBuilder = new Regex();
        for (String path : listOfFilesToBeAdded) {
            regexBuilder.addFilePath(path);
        }
        String regex = regexBuilder.buildRegex();

        boolean changedStagingArea = false;
        for (StatusFileDTO statusFileDTO : statusFileDTOs) {
            if (statusFileDTO.getPath().matches(regex)) {
                String relativePath = statusFileDTO.getPath().substring(mainDirectoryPath.length() + 1);
                String[] pathComponents = relativePath.split("\\\\");
                changedStagingArea = true;
                addNodesInPath(pathComponents, mainDirectoryPath, root, statusFileDTO, directoriesState);
            }

        }
        if (changedStagingArea) {
            commitService.addCommitTreeRoot(root);
        }
    }

    private void addNodesInPath(String[] pathComponents, String mainDirectoryPath, CommitDirectory currentDirectory, StatusFileDTO statusFileDTO, Map<String, FileStatusEnum> directoriesState) throws Exception {
        String previousPath = mainDirectoryPath;
        String relativePath = "";
        for (String component : pathComponents) {
            previousPath = previousPath + "\\" + component;
            relativePath += "\\" + component;
            String path = relativePath;

            // get node with the current path inside commitNode list
            CommitNode commitNode = currentDirectory.getCommitNodeList()
                    .stream()
                    .filter(child -> child.getPath().equals(path))
                    .findFirst()
                    .orElse(null);

            if (isFile(previousPath)) {
                LinesContainer linesContainer = CompareFileUtil.compareFiles(previousPath, statusFileDTO);
                // new File
                if (commitNode == null) {
                    commitNode = new CommitFile(path, statusFileDTO.getFileStatus(), linesContainer.getLineNumbers(), linesContainer.getNewLines(), linesContainer.getOldLines());
                    currentDirectory.addNode(commitNode);
                } else {
                    ((CommitFile) commitNode).updateCommitList(linesContainer);
                }
                break;
            } else {
                // new Directory
                if (commitNode == null) {
                    FileStatusEnum directoryState = directoriesState.getOrDefault(previousPath, FileStatusEnum.IS_MODIFIED);
                    commitNode = new CommitDirectory(path, directoryState);
                }
                currentDirectory.addNode(commitNode);
                currentDirectory = (CommitDirectory) commitNode;
            }
        }
    }

    private CommitDirectory createAndGetCommitTreeRoot(String path) throws Exception {
        CommitDirectory root = (CommitDirectory) stateTreeService.getStagingArea(path);
        if (root == null) {
            root = new CommitDirectory("", FileStatusEnum.IS_MODIFIED);
        }
        return root;
    }

    private boolean isFile(String path) {
        return new File(path).isFile();
    }

    public void unstage(String mainDirectoryPath, List<String> filesToUnstage) throws Exception {
        StateRoot stateRoot = stateTreeService.getStateRoot(mainDirectoryPath);

        NinjaNode currentNinjaNode = stateRoot.getCurrentCommit();
        if (currentNinjaNode == null) {
            currentNinjaNode = stateRoot.getCurrentBranch();
        }
        CommitDirectory commitTreeRoot = currentNinjaNode.getNextCommit().getCommitTreeRoot();

        Regex regexBuilder = new Regex();
        for (String path : filesToUnstage) {
            regexBuilder.addFilePath(path);
        }
        String regex = regexBuilder.buildRegex();

        unstageFiles(commitTreeRoot, regex);
    }

    private void unstageFiles(CommitNode commitNode, String regex) {
        List<CommitNode> commitNodeList = new ArrayList<>();
        if (commitNode instanceof CommitDirectory) {
            commitNode.setPath(commitNode.getPath() + "\\");
            commitNodeList = ((CommitDirectory) commitNode).getCommitNodeList();
        }
        if (commitNode.getFullPath().matches(regex)) {
            commitNodeRepository.delete(commitNode);

        } else {
            for (CommitNode commitNodeChild : commitNodeList) {
                unstageFiles(commitNodeChild, regex);
            }
        }
    }
}