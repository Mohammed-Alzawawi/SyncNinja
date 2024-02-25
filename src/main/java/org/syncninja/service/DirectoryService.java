package org.syncninja.service;

import org.syncninja.model.Branch;
import org.syncninja.model.Directory;
import org.syncninja.repository.DirectoryRepository;

public class DirectoryService {
    DirectoryRepository directoryRepository = new DirectoryRepository();

    public Directory createDirectory(String path) throws Exception {
        if(directoryRepository.findById(path).isPresent()){
            throw new Exception("directory exists");
        }
        return directoryRepository.save(new Directory(path));
    }

    public void createDirectoryMainBranch(Directory directory, String name){
        if(directory.getBranch() == null){
            directory.setBranch(new Branch(name));
            directoryRepository.save(directory);
        }
    }

    public Directory getDirectory(String path) throws Exception {
        return directoryRepository.findById(path).orElseThrow(()->
                new Exception("directory not init"));
    }
}
