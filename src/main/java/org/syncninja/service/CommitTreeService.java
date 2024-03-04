package org.syncninja.service;

import org.syncninja.dto.StatusFileDTO;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitFile;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.util.CompareFileUtil;
import org.syncninja.util.FileTrackingState;
import org.syncninja.util.LinesContainer;
import org.syncninja.util.ResourceBundleEnum;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CommitTreeService {
    private final StatusService statusService;
    private final CommitNodeRepository commitNodeRepository;

    public CommitTreeService() {
        this.statusService = new StatusService();
        this.commitNodeRepository = new CommitNodeRepository();
    }

    public void addFilesFromDirectoryToCommitTree(String directoryPath) throws Exception {
        FileTrackingState fileTrackingState = statusService.getState(directoryPath);
        List<StatusFileDTO> untrackedFiles = fileTrackingState.getUntracked();
        addFilesToCommitTree(untrackedFiles, directoryPath);
    }

    public void addFileToCommitTree(String mainDirectoryPath, String filePath) throws Exception {
        if (!Files.isRegularFile(Path.of(filePath))) {
            throw new IllegalArgumentException(ResourceMessagingService.getMessage(ResourceBundleEnum.PATH_NOT_FILE, new Object[]{filePath}));
        }
        FileTrackingState fileTrackingState = statusService.getState(mainDirectoryPath);
        List<StatusFileDTO> untrackedFiles = fileTrackingState.getUntracked();
        addFilesToCommitTree(untrackedFiles, mainDirectoryPath, filePath);

    }

    private void addFilesToCommitTree(List<StatusFileDTO> statusFileDTOs, String mainDirectoryPath) throws Exception {
        CommitNode root = new CommitDirectory(mainDirectoryPath);

        for (StatusFileDTO statusFileDTO : statusFileDTOs) {
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
        commitNodeRepository.save(root);
    }

    private void addFilesToCommitTree(List<StatusFileDTO> statusFileDTOs, String mainDirectoryPath, String filePath) throws Exception {
        CommitNode root = new CommitDirectory(mainDirectoryPath);
        filePath = filePath.replace("\\\\", "\\");


        for (StatusFileDTO statusFileDTO : statusFileDTOs) {

            System.out.println(statusFileDTO.getPath());
            System.out.println(filePath);
            if (!statusFileDTO.getPath().equals(filePath)){
                continue;
            }
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
        commitNodeRepository.save(root);
    }

    private boolean isFile(String path) {
        return new File(path).isFile();
    }
}

