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

    public void addFilesFromDirectoryToCommitTree(String directoryPath) throws Exception {
        FileTrackingState fileTrackingState = statusService.getState(directoryPath);
        List<StatusFileDTO> untrackedFiles = fileTrackingState.getUntracked();
        addFilesToCommitTree(untrackedFiles, directoryPath);
    }

    private void addFilesToCommitTree(List<StatusFileDTO> untrackedFiles, String mainDirectoryPath) throws Exception {
        CommitDirectory root = new CommitDirectory(mainDirectoryPath);
        commitService.addCommitTree(mainDirectoryPath, root);
        for (StatusFileDTO statusFileDTO : untrackedFiles) {
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

    private boolean isFile(String path) {
        return new File(path).isFile();
    }
}