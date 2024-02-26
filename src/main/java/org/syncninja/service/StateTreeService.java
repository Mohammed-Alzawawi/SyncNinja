package org.syncninja.service;

import org.syncninja.Utilities.ResourceBundleEnum;
import org.syncninja.model.StateDirectory;
import org.syncninja.model.StateFile;
import org.syncninja.repository.StateDirectoryRepository;
import org.syncninja.repository.StateFileRepository;

import java.io.IOException;
import java.util.Optional;

public class StateTreeService {
    private final StateDirectoryRepository stateDirectoryRepository = new StateDirectoryRepository();
    private final ResourceMessagingService resourceMessagingService = new ResourceMessagingService();
    private final StateFileRepository stateFileRepository = new StateFileRepository();

    public StateFile generateStateFileNode(String path) throws Exception {
        StateFile file = null;
        if(stateFileRepository.existsById(path)){
            throw new Exception(resourceMessagingService.getMessage(ResourceBundleEnum.FILE_ALREADY_EXISTS, new Object[]{path}));
        }
        else{
            file = new StateFile(path);
            stateFileRepository.save(file);
        }
        return file;
    }

    public StateDirectory generateStateDirectoryNode(String path) throws Exception {
        StateDirectory stateDirectory = null;
        if(stateDirectoryRepository.existsById(path)){
            throw new Exception(resourceMessagingService.getMessage(ResourceBundleEnum.SUB_DIRECTORY_ALREADY_EXISTS, new Object[]{path}));
        }
        else{
            stateDirectory = new StateDirectory(path);
            stateDirectoryRepository.save(stateDirectory);
        }
        return stateDirectory;

    }




}