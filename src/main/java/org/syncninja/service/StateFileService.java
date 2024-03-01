package org.syncninja.service;

import org.syncninja.model.StateFile;
import org.syncninja.repository.StateFileRepository;
import org.syncninja.util.ResourceBundleEnum;

public class StateFileService {
    StateFileRepository stateFileRepository;

    public StateFileService() {
        this.stateFileRepository = stateFileRepository;
    }

    public StateFile getStateFile(String path) throws Exception {
        return stateFileRepository.findById(path).orElseThrow(()->
                new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.FILE_NOT_FOUND, new Object[]{path})));
    }
}
