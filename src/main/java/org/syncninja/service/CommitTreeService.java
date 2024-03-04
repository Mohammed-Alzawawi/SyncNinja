package org.syncninja.service;

import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitFile;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.util.FileState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class CommitTreeService {
    private final StatusService statusService;
    private final CommitNodeRepository commitNodeRepository;

    public CommitTreeService() {
        this.statusService = new StatusService();
        this.commitNodeRepository = new CommitNodeRepository();
    }
    public void addFilesFromDirectoryToCommitTree(String directoryPath) throws Exception {
        FileState fileState = statusService.getStatus(directoryPath);
        List<String> untracedFiles =fileState.getUntracked();
        addFilesToCommitTree(untracedFiles,directoryPath);
    }
    //all
    public void addDirectoryToCommitTree(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The provided path is not a directory: " + directoryPath);
        }
        List<String> filePaths = Files.walk(Path.of(directoryPath))
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .toList();

        addFilesToCommitTree(filePaths, directoryPath);

        // (/user)
        File[] subDirectories = directory.listFiles(File::isDirectory);
        if (subDirectories != null) {
            for (File subDirectory : subDirectories) {
                addDirectoryToCommitTree(subDirectory.getAbsolutePath());
            }
        }
    }
    public void addFileToCommitTree(String filePath) {
        if (!Files.isRegularFile(Path.of(filePath))) {
            throw new IllegalArgumentException("The provided path is not a regular file: " + filePath);
        }
        addFilesToCommitTree(Collections.singletonList(filePath), new File(filePath).getParent());
    }
    private void addFilesToCommitTree(List<String> filePaths, String mainDirectoryPath) {
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
    }
    private boolean isFile(String path) {
        return new File(path).isFile();
    }

}

