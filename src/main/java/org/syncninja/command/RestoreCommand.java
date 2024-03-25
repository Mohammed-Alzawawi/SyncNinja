package org.syncninja.command;

import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "restore")
public class RestoreCommand implements Runnable {
    private final StateTreeService stateTreeService;

    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files to restore")
    private final List<String> listOfFilesToRestore = new ArrayList<>();

    public RestoreCommand() {
        stateTreeService = new StateTreeService();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        try{
            stateTreeService.restore(listOfFilesToRestore, path);
            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.RESTORED_SUCCESSFULLY));
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}