package org.syncninja.service;

import org.syncninja.model.Branch;
import org.syncninja.model.Directory;
import org.syncninja.repository.DirectoryRepository;
import org.syncninja.util.ResourceBundleEnum;

public class DirectoryService {
    private final DirectoryRepository directoryRepository;

    public DirectoryService() {
        this.directoryRepository = new DirectoryRepository();
    }

    public Directory createDirectory(String path) throws Exception {
        if (directoryRepository.findByPath(path).isPresent()) {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_ALREADY_INITIALIZED, new Object[]{path}));
        }
        return directoryRepository.save(new Directory(path));
    }

    public void createDirectoryMainBranch(Directory directory, String name) {
        if (directory.getBranch() == null) {
            directory.setBranch(new Branch(name));
            directoryRepository.save(directory);
        }
    }

    public Directory getDirectory(String path) throws Exception {
        return directoryRepository.findByPath(path).orElseThrow(() ->
                new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path})));
    }
}