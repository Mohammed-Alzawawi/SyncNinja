package org.syncninja.service;

import org.syncninja.model.Branch;
import org.syncninja.model.StateTree.StateDirectory;
import org.syncninja.model.StateTree.StateFile;
import org.syncninja.model.StateTree.StateRoot;
import org.syncninja.model.StateTree.StateTree;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.ResourceBundleEnum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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

    public void generateStateTree(String path) throws Exception {
        Path mainDirectory = Paths.get(path);
        List<Path> subList = null;
        try {
            subList = Files.walk(mainDirectory)
                    .collect(Collectors.toList());
            {
                for (int i = 1; i < subList.size(); i++) {
                    Path file = subList.get(i);
                    if (file.toFile().isDirectory()) {
                        StateDirectory child = generateStateDirectoryNode(file.toString());
                        StateDirectory parent = (StateDirectory) stateTreeRepository.findById(file.getParent().toString()).orElse(null);
                        if (parent != null) {
                            parent.addFile(child);
                            stateTreeRepository.save(parent);
                        }
                    } else {
                        StateFile child = generateStateFileNode(file.toString());
                        StateDirectory parent = (StateDirectory) stateTreeRepository.findById(file.getParent().toString()).orElse(null);

                        if (parent != null) {
                            parent.addFile(child);
                            stateTreeRepository.save(parent);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StateTree getStateNode(String path) throws Exception {
        return stateTreeRepository.findById(path).orElseThrow(() ->
                new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.FILE_NOT_FOUND, new Object[]{path})));
    }
<<<<<<<HEAD

    public StateRoot generateStateRootNode(String path, Branch currentBranch) throws Exception {
        StateRoot stateRoot = null;
        if (!stateTreeRepository.findById(path).isEmpty()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_ALREADY_INITIALIZED, new Object[]{path}));
        } else {
            stateRoot = new StateRoot(path, currentBranch);
            stateTreeRepository.save(stateRoot);
        }
        return stateRoot;
    }

=======
        >>>>>>>fff78decc915b2df9743a12eeb0268468196c509
}