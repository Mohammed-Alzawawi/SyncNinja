package org.syncninja.service;

import org.syncninja.dto.StatusFileDTO;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitFile;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.util.CompareFileUtil;
import org.syncninja.util.FileTrackingState;
import org.syncninja.util.LinesContainer;
import org.syncninja.util.Regex;
import org.syncninja.util.ResourceBundleEnum;

import java.io.File;
import java.util.List;

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

    public CommitNode getCommitTreeRoot(String path) throws Exception {
        return commitNodeRepository.findByPath(path).orElseThrow(
                () -> new RuntimeException(ResourceMessagingService.getMessage(ResourceBundleEnum.STAGE_AREA_IS_EMPTY)));
    }
}