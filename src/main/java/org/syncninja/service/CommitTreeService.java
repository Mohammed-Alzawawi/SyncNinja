//package org.syncninja.service;
//
//import org.syncninja.model.commitTree.CommitDirectory;
//import org.syncninja.model.commitTree.CommitFile;
//import org.syncninja.model.commitTree.CommitNode;
//import org.syncninja.repository.CommitNodeRepository;
//import org.syncninja.util.Fetcher;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.List;
//
//public class CommitTreeService {
//    private final StatusService statusService;
//    private final CommitNodeRepository commitNodeRepository;
//
//    public CommitTreeService(StatusService statusService, CommitNodeRepository commitNodeRepository) {
//        this.statusService = statusService;
//        this.commitNodeRepository = commitNodeRepository;
//    }
//
//    public void addFilesFromDirectoryToCommitTree(String directoryPath) throws IOException {
//        List<String> filePaths = Fetcher.pathList(Path.of(directoryPath));
//        List<String> untrackedFiles = statusService.getUntrackedFiles(filePaths, directoryPath);
//        addFilesToCommitTree(untrackedFiles, directoryPath);
//    }
//
//    public void addFilesToCommitTree(List<String> filePaths, String mainDirectoryPath) {
//        CommitNode root = new CommitDirectory(mainDirectoryPath);
//
//        for (String path : filePaths) {
//            String relativePath = path.substring(mainDirectoryPath.length() + 1);
//            String[] pathComponents = relativePath.split("\\\\");
//            CommitNode currentNode = root;
//            String previousPath = mainDirectoryPath;
//
//            for (String component : pathComponents) {
//                previousPath = previousPath + "\\" + component;
//                boolean found = false;
//                if (currentNode instanceof CommitDirectory && ((CommitDirectory) currentNode).getCommitNodeList() != null) {
//                    for (CommitNode child : ((CommitDirectory) currentNode).getCommitNodeList()) {
//                        if (child.getPath().equals(previousPath)) {
//                            currentNode = child;
//                            found = true;
//                            break;
//                        }
//                    }
//                }
//                if (!found) {
//                    CommitNode newNode;
//                    if (isFile(previousPath)) {
//                        newNode = new CommitFile(previousPath);
//                    } else {
//                        newNode = new CommitDirectory(previousPath);
//                    }
//                    ((CommitDirectory) currentNode).addNode(newNode);
//                    currentNode = newNode;
//                }
//            }
//        }
//        commitNodeRepository.save(root);
//    }
//
//    private boolean isFile(String path) {
//        return new File(path).isFile();
//    }
//}
