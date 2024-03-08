package org.syncninja.service;

import org.syncninja.dto.StatusFileDTO;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitFile;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.util.CompareFileUtil;
import org.syncninja.util.FileTrackingState;
import org.syncninja.util.LinesContainer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommitTreeService {
    private final StatusService statusService;
    private final CommitNodeRepository commitNodeRepository;
    private final CommitService commitService;

    public CommitTreeService() {
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
        CommitNode root = new CommitDirectory(mainDirectoryPath);

        String regex = "";
        for (String path : listOfFilesToBeAdded) {
            if (path.endsWith(".") ){
                path = path +"*";
            }
            regex += "|" + path;
        }

        for (StatusFileDTO statusFileDTO : statusFileDTOs) {
            if (!statusFileDTO.getPath().matches(regex) && !listOfFilesToBeAdded.isEmpty()){
                continue;
            }
        }

        for (StatusFileDTO statusFileDTO : statusFileDTOs) {
            if (!statusFileDTO.getPath().matches(regex) && !listOfFilesToBeAdded.isEmpty()) {continue;}

  
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
                    ((CommitDirectory) currentNode).getCommitNodeList().add(newNode);
                    currentNode = newNode;
                }
            }
        }
        commitNodeRepository.save(root);
    }

    private void removeFileFromCommitTree(CommitNode currentNode, String filePath) {
        if (currentNode instanceof CommitFile && currentNode.getPath().equals(filePath)) {
            ((CommitDirectory) currentNode.getParent()).removeNode(currentNode);
            return;
        }
        if (currentNode instanceof CommitDirectory) {
            for (CommitNode child : ((CommitDirectory) currentNode).getCommitNodeList()) {
                removeFileFromCommitTree(child, filePath);
            }
        }
    }
    private CommitNode getOrCreateStagingArea(String mainDirectoryPath) {
        Optional<CommitNode> optionalRoot = commitNodeRepository.findByPath(mainDirectoryPath);
        CommitNode root = optionalRoot.orElseGet(() -> {
            CommitDirectory newRoot = new CommitDirectory(mainDirectoryPath);
            commitNodeRepository.save(newRoot);
            return newRoot;
        });
        return root;
    }
    private boolean isFile(String path) {
        return new File(path).isFile();
    }
}