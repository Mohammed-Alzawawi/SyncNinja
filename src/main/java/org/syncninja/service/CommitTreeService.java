package org.syncninja.service;

import org.syncninja.dto.StatusFileDTO;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.StateTree.StateRoot;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitFile;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.util.CompareFileUtil;
import org.syncninja.util.FileTrackingState;
import org.syncninja.util.LinesContainer;
import org.syncninja.util.Regex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
            throw new IllegalStateException("Failed to retrieve file tracking state for directory: " + mainDirectoryPath);
        }
        List<StatusFileDTO> untrackedFiles = fileTrackingState.getUntracked();
        addFilesToCommitTree(untrackedFiles, mainDirectoryPath, listOfFilesToBeAdded);
    }

    private void addFilesToCommitTree(List<StatusFileDTO> statusFileDTOs, String mainDirectoryPath, List<String> listOfFilesToBeAdded) throws Exception {
        CommitDirectory root = new CommitDirectory(mainDirectoryPath);
        commitService.addCommitTree(root);

        Regex regexBuilder = new Regex();
        for (String path : listOfFilesToBeAdded) {
            regexBuilder.addFilePath(path);
        }
        String regex = regexBuilder.buildRegex();

        for (StatusFileDTO statusFileDTO : statusFileDTOs) {
            if (statusFileDTO.getPath().matches(regex)) {
                String relativePath = statusFileDTO.getPath().substring(mainDirectoryPath.length() + 1);
                String[] pathComponents = relativePath.split("\\\\");
                CommitNode currentNode = root;
                String previousPath = mainDirectoryPath;

                for (String component : pathComponents) {
                    previousPath = previousPath + "\\" + component;
                    boolean found = false;
                    if (currentNode instanceof CommitDirectory && ((CommitDirectory) currentNode).getCommitNodeList() != null) {
                        for (CommitNode child : ((CommitDirectory) currentNode).getCommitNodeList()) {
                            if (child.getPath().equals(previousPath)) {
                                currentNode = child;
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        CommitNode newNode;
                        if (isFile(previousPath)) {
                            LinesContainer linesContainer = CompareFileUtil.compareFiles(previousPath, statusFileDTO);
                            newNode = new CommitFile(previousPath, linesContainer.getLineNumbers(), linesContainer.getNewLines(), linesContainer.getOldLines());
                        } else {
                            newNode = new CommitDirectory(previousPath);
                        }
                        ((CommitDirectory) currentNode).addNode(newNode);
                        currentNode = newNode;
                    }
                }
            }

        }
        commitNodeRepository.save(root);
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
        CommitDirectory commitTreeRoot = currentNinjaNode.getNextCommit().getCommitTree();

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
        if (commitNode.getPath().matches(regex)) {
            commitNodeRepository.delete(commitNode);

        } else {
            for (CommitNode commitNodeChild : commitNodeList) {
                unstageFiles(commitNodeChild, regex);
            }
        }
    }
}