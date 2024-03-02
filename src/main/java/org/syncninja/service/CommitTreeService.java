package org.syncninja.service;

import org.syncninja.model.NinjaNode;
import org.syncninja.model.SyncNode;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitFile;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.util.FileState;
import org.syncninja.util.ResourceBundleEnum;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class CommitTreeService {
    private final StatusService statusService;
    private final CommitNodeRepository commitNodeRepository;

    public CommitTreeService() {
        this.statusService = new StatusService();
        this.commitNodeRepository = new CommitNodeRepository();
    }
    public CommitNode addFilesFromDirectoryToCommitTree(String directoryPath) throws Exception {
        FileState fileState = statusService.getStatus(directoryPath);
        List<String> untracedFiles =fileState.getUntracked();
        CommitNode commitNode = addFilesToCommitTree(untracedFiles,directoryPath);
        return commitNode;
    }
    private CommitNode addFilesToCommitTree(List<String> filePaths, String mainDirectoryPath) {
        CommitNode root = new CommitDirectory(mainDirectoryPath);

        for (String path : filePaths) {
            String relativePath = path.substring(mainDirectoryPath.length() + 1);
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
                        newNode = new CommitFile(previousPath);
                    } else {
                        newNode = new CommitDirectory(previousPath);
                    }
                    ((CommitDirectory) currentNode).addNode(newNode);
                    currentNode = newNode;
                }
            }
        }
        commitNodeRepository.save(root);
        return root;
    }
    private boolean isFile(String path) {
        return new File(path).isFile();
    }


}
