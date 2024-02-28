package org.syncninja.service;

import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.ResourceBundleEnum;
import org.syncninja.model.StateDirectory;
import org.syncninja.model.StateFile;
import org.syncninja.repository.StateDirectoryRepository;
import org.syncninja.repository.StateFileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class StateTreeService {
    private final StateDirectoryRepository stateDirectoryRepository = new StateDirectoryRepository();
    private final ResourceMessagingService resourceMessagingService = new ResourceMessagingService();
    private final StateFileRepository stateFileRepository = new StateFileRepository();
    private final StateTreeRepository stateTreeRepository = new StateTreeRepository();

    public StateFile generateStateFileNode(String path) throws Exception {
        StateFile file = null;
        if(!stateFileRepository.findById(path).isEmpty()){
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
        if(!stateDirectoryRepository.findById(path).isEmpty()){
            throw new Exception(resourceMessagingService.getMessage(ResourceBundleEnum.SUB_DIRECTORY_ALREADY_EXISTS, new Object[]{path}));
        }
        else{
            stateDirectory = new StateDirectory(path);
            stateDirectoryRepository.save(stateDirectory);
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
                for(int i = 0 ; i<subList.size() ; i++){
                    Path file = subList.get(i);
                    if(file.toFile().isDirectory()){
                        StateDirectory child = generateStateDirectoryNode(file.toString());
                        StateDirectory parent = stateDirectoryRepository.findById(file.getParent().toString()).orElse(null);
                        if(parent!=null){
                            parent.addfile(child);
                            stateDirectoryRepository.save(parent);
                        }
                    }
                    else{
                        StateFile child = generateStateFileNode(file.toString());
                        StateDirectory parent = stateDirectoryRepository.findById(file.getParent().toString()).orElse(null);

                        if(parent!=null){
                            parent.addfile(child);
                            stateDirectoryRepository.save(parent);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}