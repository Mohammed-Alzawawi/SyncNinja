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

public class StateTreeService {
    private final StateTreeRepository stateTreeRepository;

    public StateTreeService() {
        stateTreeRepository = new StateTreeRepository();
    }

    public StateFile generateStateFileNode(String path) throws Exception {
        StateFile file = null;
        if (stateTreeRepository.findById(path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.FILE_ALREADY_EXISTS, new Object[]{path}));
        } else {
            file = new StateFile(path);
            stateTreeRepository.save(file);
        }
        StateDirectory parent = (StateDirectory) stateTreeRepository.findById(new File(path).getParent().toString()).orElse(null);
        if (parent == null) {
            parent = new StateDirectory(new File(path).getParent().toString());
        }
        parent.getInternalNodes().add(file);
        stateTreeRepository.save(parent);
        return file;
    }

    public StateDirectory generateStateDirectoryNode(String path) throws Exception {
        StateDirectory stateDirectory = null;
        if (stateTreeRepository.findById(path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.SUB_DIRECTORY_ALREADY_EXISTS, new Object[]{path}));
        } else {
            stateDirectory = new StateDirectory(path);
            stateTreeRepository.save(stateDirectory);
        }
        return stateDirectory;
    }

    public StateTree getStateNode(String path) throws Exception {
        return stateTreeRepository.findById(path).orElseThrow(() ->
                new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.FILE_NOT_FOUND, new Object[]{path})));
    }


    public StateRoot generateStateRootNode(String path, Branch currentBranch) throws Exception {
        StateRoot stateRoot = null;
        if (stateTreeRepository.findById(path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_ALREADY_INITIALIZED, new Object[]{path}));
        } else {
            stateRoot = new StateRoot(path, currentBranch);
            stateTreeRepository.save(stateRoot);
        }
        return stateRoot;
    }

    public StateRoot getStateRoot(String path) throws Exception {
        return (StateRoot) stateTreeRepository.findById(path).orElseThrow(
                () -> new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path})));
    }

    public void updateStateRoot(StateRoot stateRoot, Commit newCommit) {
        stateTreeRepository.updateStateRoot(stateRoot, newCommit);
    }
}