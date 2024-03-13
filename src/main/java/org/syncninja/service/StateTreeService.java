package org.syncninja.service;

import org.syncninja.model.Branch;
import org.syncninja.model.Commit;
import org.syncninja.model.StateTree.StateDirectory;
import org.syncninja.model.StateTree.StateFile;
import org.syncninja.model.StateTree.StateRoot;
import org.syncninja.model.StateTree.StateTree;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.ResourceBundleEnum;

import java.io.File;
import java.io.IOException;

public class StateTreeService {
    private final StateTreeRepository stateTreeRepository;

    public StateTreeService() {
        stateTreeRepository = new StateTreeRepository();
    }

    public StateFile generateStateFileNode(String path) throws Exception {
        StateFile file = (StateFile) stateTreeRepository.findById(path)
                .orElseGet(() -> {
                    StateFile newFile = null;
                    try {
                        newFile = new StateFile(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    stateTreeRepository.save(newFile);
                    return newFile;
                });
        StateDirectory parent = (StateDirectory) stateTreeRepository.findById(new File(path).getParent()).orElse(null);
        if (parent == null) {
            parent = new StateDirectory(new File(path).getParent());
        }
        parent.getInternalNodes().add(file);
        stateTreeRepository.save(parent);
        return file;
    }

    public StateDirectory generateStateDirectoryNode(String path) throws Exception {
        return (StateDirectory) stateTreeRepository.findById(path).orElseGet(() -> {
            StateDirectory newDirectory = null;
            newDirectory = new StateDirectory(path);
            stateTreeRepository.save(newDirectory);
            return newDirectory;
        });
    }

    public StateTree getStateNode(String path) throws Exception {
        return stateTreeRepository.findById(path).orElseThrow(() ->
                new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.FILE_NOT_FOUND, new Object[]{path})));
    }


    public void generateStateRootNode(String path, Branch currentBranch) throws Exception {
        StateRoot stateRoot = null;
        if (stateTreeRepository.findById(path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_ALREADY_INITIALIZED, new Object[]{path}));
        } else {
            stateRoot = new StateRoot(path, currentBranch);
            stateTreeRepository.save(stateRoot);
        }
    }

    public StateRoot getStateRoot(String path) throws Exception {
        return (StateRoot) stateTreeRepository.findById(path).orElseThrow(
                () -> new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path})));
    }

    public void updateStateRoot(StateRoot stateRoot, Commit newCommit) {
        stateTreeRepository.updateStateRoot(stateRoot, newCommit);
    }

    public void updateStateTree(String path) throws Exception {
        StateRoot stateRoot = getStateRoot(path);

        if (stateRoot != null) {
            File mainDirectory = new File(path);
            generateNodes(mainDirectory, null, stateRoot);
        }
    }

    private void generateNodes(File directory, StateDirectory parent, StateRoot stateRoot) throws Exception {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    StateDirectory stateDirectory = generateStateDirectoryNode(file.getAbsolutePath());
                    if (parent != null) {
                        parent.addFile(stateDirectory);
                        stateTreeRepository.save(parent);
                    } else {
                        stateRoot.addFile(stateDirectory);
                        stateTreeRepository.save(stateRoot);
                    }
                    generateNodes(file, stateDirectory, stateRoot);
                } else {
                    StateFile stateFile = generateStateFileNode(file.getAbsolutePath());
                    if (parent != null) {
                        parent.addFile(stateFile);
                        stateTreeRepository.save(parent);
                    } else {
                        stateRoot.addFile(stateFile);
                        stateTreeRepository.save(stateRoot);
                    }
                }
            }
        }
    }
}