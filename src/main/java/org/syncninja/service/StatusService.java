package org.syncninja.service;

import org.syncninja.dto.StatusFileDTO;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.model.StateDirectory;
import org.syncninja.model.StateFile;
import org.syncninja.model.StateTree;
import org.syncninja.util.FileTrackingState;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatusService {
    private final StateTreeRepository stateTreeRepository;

    public StatusService() {
        stateTreeRepository = new StateTreeRepository();
    }

    public void currentState(File directory, StateDirectory stateDirectory, List<StatusFileDTO> untracked) {
        File[] filesList = directory.listFiles();
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
                if (stateFile == null){
                    untracked.add(new StatusFileDTO(true, null, file.getPath()));
                } else {
                    untracked.add(new StatusFileDTO(false, stateFile, file.getPath()));
                }
            }
        }
    }

    private void addAllFilesInDirectory(File directory, List<StatusFileDTO> untracked) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                addAllFilesInDirectory(file, untracked);
            } else if (file.isFile()) {
                untracked.add(new StatusFileDTO(true, null,file.getPath()));
            }
        }
    }

    public FileTrackingState getState(String path) throws Exception {
        if (stateTreeRepository.findById(path).isEmpty()) {
            return null;
        }
        FileTrackingState fileTrackingState = new FileTrackingState();
        StateDirectory stateDirectory = (StateDirectory) stateTreeRepository.findById(path).orElse(null);
        currentState(new File(path), stateDirectory, fileTrackingState.getUntracked());
        return fileTrackingState;
    }
}