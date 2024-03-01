package org.syncninja.service;

import org.syncninja.repository.StateTreeRepository;
import org.syncninja.model.StateDirectory;
import org.syncninja.model.StateFile;
import org.syncninja.model.StateTree;
import org.syncninja.repository.StateDirectoryRepository;
import org.syncninja.repository.StateFileRepository;
import org.syncninja.util.ResourceBundleEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatusService {

    private final StateDirectoryRepository stateDirectoryRepository = new StateDirectoryRepository();
    private final ResourceMessagingService resourceMessagingService = new ResourceMessagingService();
    private final StateFileRepository stateFileRepository = new StateFileRepository();
    private final StateTreeRepository stateTreeRepository = new StateTreeRepository();
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

    public Object[] getStatus(String path) throws Exception {

        if (stateTreeRepository.findById(path)==null) {
            //System.out.println(path);
            throw new Exception(resourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path}));
        }
        List<String> tracked = new ArrayList<>();
        List<String> untracked = new ArrayList<>();

        StateDirectory stateDirectory = (StateDirectory) stateTreeRepository.findById(path);

        currentState(new File(path), stateDirectory, untracked);
        return new Object[]{tracked, untracked};
    }
}
