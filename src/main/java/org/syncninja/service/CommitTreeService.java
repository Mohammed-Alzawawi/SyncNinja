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
        CommitDirectory root = createAndGetCommitTreeRoot(mainDirectoryPath);

        // building regex for add command
        Regex regexBuilder = new Regex();
        for (String path : listOfFilesToBeAdded) {
            regexBuilder.addFilePath(path);
        }
        String regex = regexBuilder.buildRegex();


        for (StatusFileDTO statusFileDTO : statusFileDTOs) {
            if (statusFileDTO.getPath().matches(regex)) {
                String relativePath = statusFileDTO.getPath().substring(mainDirectoryPath.length() + 1);
                String[] pathComponents = relativePath.split("\\\\");

                addNodesInPath(pathComponents, mainDirectoryPath, root, statusFileDTO);
            }

        }
        commitNodeRepository.save(root);
    }

    private void addNodesInPath(String[] pathComponents, String mainDirectoryPath, CommitDirectory currentDirectory, StatusFileDTO statusFileDTO) throws Exception {
        String previousPath = mainDirectoryPath;

        for (String component : pathComponents) {
            previousPath = previousPath + "\\" + component;
            String path = previousPath;

            // get node with the current path inside commitNode list
            CommitNode commitNode =  currentDirectory.getCommitNodeList()
                    .stream()
                    .filter(child -> child.getPath().equals(path))
                    .findFirst()
                    .orElse(null);

            if(isFile(path)) {
                LinesContainer linesContainer = CompareFileUtil.compareFiles(path, statusFileDTO);
                // new File
                if(commitNode == null) {
                    commitNode = new CommitFile(path, linesContainer.getLineNumbers(), linesContainer.getNewLines(), linesContainer.getOldLines());
                    currentDirectory.addNode(commitNode);
                } else {
                    ((CommitFile) commitNode).updateCommitList(linesContainer);
                }
                break;
            } else {
                // new Directory
                if(commitNode == null) {
                    commitNode = new CommitDirectory(path);
                }
                currentDirectory.addNode(commitNode);
                currentDirectory = (CommitDirectory) commitNode;
            }
        }
    }
    private CommitDirectory createAndGetCommitTreeRoot(String path) throws Exception {
        CommitDirectory root = (CommitDirectory) stateTreeService.getStagingArea(path);
        if(root == null) {
            root = new CommitDirectory(path);
        }
        commitService.addCommitTree(root);
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