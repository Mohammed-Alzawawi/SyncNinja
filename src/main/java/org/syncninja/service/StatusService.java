package org.syncninja.service;

import org.syncninja.repository.StateTreeRepository;
import org.syncninja.model.StateDirectory;
import org.syncninja.model.StateFile;
import org.syncninja.model.StateTree;
import org.syncninja.util.FileState;
import org.syncninja.util.ResourceBundleEnum;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatusService {
    private final StateTreeRepository stateTreeRepository;

    public StatusService() {
        stateTreeRepository = new StateTreeRepository();
    }

    public void currentState(File directory, StateDirectory stateDirectory, List<String> untracked) {
        File filesList[] = directory.listFiles();
        Map<String, StateTree> stateTreeMap = stateDirectory.getInternalNodes().stream()
                .collect(Collectors.toMap((stateTree) -> stateTree.getPath(), (stateTree -> stateTree)));

        for (File file: filesList) {
            if (file.isDirectory()) {
                StateDirectory stateDirectoryChild = (StateDirectory) stateTreeMap.get(file.getPath());
                if(stateDirectoryChild == null) {
                    addAllFilesInDirectory(file, untracked);

                } else if (stateDirectoryChild.getLastModified() != file.lastModified()) {
                    currentState(file, stateDirectoryChild, untracked);
                }
            } else {
                StateFile stateFile = (StateFile) stateTreeMap.get(file.getPath());
                if (stateFile == null || stateFile.getLastModified() != file.lastModified()) {
                    untracked.add(file.toString());
                }
            }
        }
    }

    private void addAllFilesInDirectory(File directory, List<String> untracked) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                addAllFilesInDirectory(file, untracked);
            } else if (file.isFile()) {
                untracked.add(file.getPath());
            }
        }
    }

    public FileState getStatus(String path) throws Exception {
        if (stateTreeRepository.findById(path).isEmpty()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path}));
        }

        FileState fileStatus = new FileState();
        StateDirectory stateDirectory = (StateDirectory) stateTreeRepository.findById(path).orElse(null);
        currentState(new File(path), stateDirectory, fileStatus.getUntracked());
        return fileStatus;
    }
}
