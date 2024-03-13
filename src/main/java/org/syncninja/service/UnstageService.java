package org.syncninja.service;

import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.util.Regex;

import java.util.List;
import java.util.Optional;

public class UnstageService {
    private final CommitNodeRepository commitNodeRepository;
    private final StatusService statusService;

    public UnstageService() {
        this.commitNodeRepository = new CommitNodeRepository();
        this.statusService = new StatusService();
    }

    public void unstageFiles(String mainDirectoryPath, List<String> listOfFilesToUnstage) {
        Optional<CommitNode> rootOptional = commitNodeRepository.findByPath(mainDirectoryPath);
        CommitNode root = rootOptional.orElseThrow(() -> new IllegalStateException("Failed to retrieve commit tree root for directory: " + mainDirectoryPath));

        CommitDirectory rootDirectory = (CommitDirectory) root;
        unstageFilesFromCommitTree(rootDirectory, mainDirectoryPath, listOfFilesToUnstage);

        commitNodeRepository.save(root);
    }

    private void unstageFilesFromCommitTree(CommitDirectory currentDirectory, String mainDirectoryPath, List<String> listOfFilesToUnstage) {
        Regex regexBuilder = new Regex();
        for (String path : listOfFilesToUnstage) {
            regexBuilder.addFilePath(path);
        }
        String regex = regexBuilder.buildRegex();

        unstageFilesUsingRegex(currentDirectory, mainDirectoryPath, regex);
    }

    private void unstageFilesUsingRegex(CommitDirectory currentDirectory, String mainDirectoryPath, String regex) {
        currentDirectory.getCommitNodeList().removeIf(node -> {
            if (node instanceof CommitNode) {
                String filePath = node.getPath();
                //should this be relative path thingy?
                if (filePath.startsWith(mainDirectoryPath) && filePath.matches(regex)) {
                    try {
                        removeNodeAndChildren(node);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }
            }
            return false;
        });
    }
    private void removeNodeAndChildren(CommitNode node) throws Exception {
        if (node instanceof CommitDirectory) {
            CommitDirectory directoryNode = (CommitDirectory) node;
            for (CommitNode child : directoryNode.getCommitNodeList()) {
                removeNodeAndChildren(child);
            }
        }
        commitNodeRepository.deleteByPath(node.getPath());
    }
}
