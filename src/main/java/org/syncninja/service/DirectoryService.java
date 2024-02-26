package org.syncninja.service;

import org.syncninja.model.Branch;
import org.syncninja.model.Directory;
import org.syncninja.repository.DirectoryRepository;
import org.syncninja.util.ResourceBundleEnum;

public class DirectoryService {
    DirectoryRepository directoryRepository = new DirectoryRepository();
    ResourceMessagingService resourceMessagingService = new ResourceMessagingService();

    public Directory createDirectory(String path) throws Exception {
        if(directoryRepository.findByPath(path).isPresent()){
            throw new Exception(resourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_ALREADY_INITIALIZED, new Object[]{path}));
        }
        System.out.println(resourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_INITIALIZED_SUCCESSFULLY, new Object[]{path}));
        return directoryRepository.save(new Directory(path));
    }

    public void createDirectoryMainBranch(Directory directory, String name){
        if(directory.getBranch() == null){
            directory.setBranch(new Branch(name));
            directoryRepository.save(directory);
        }
    }

    public Directory getDirectory(String path) throws Exception {
        return directoryRepository.findByPath(path).orElseThrow(()->
                new Exception(resourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path})));
    }
}