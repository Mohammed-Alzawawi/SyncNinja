package org.syncninja.command;

import org.syncninja.dto.StatusFileDTO;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StatusService;
import org.syncninja.util.FileState;
import picocli.CommandLine;
import org.syncninja.util.ResourceBundleEnum;
import java.util.*;

@CommandLine.Command(name = "status")
public class StatusCommand implements Runnable{
    private final StatusService statusService;

    public StatusCommand() {
        this.statusService = new StatusService();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        try {
            FileState state = statusService.getState(path);

            if(state == null){
                throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path}));
            }
            
            List<StatusFileDTO> tracked = state.getTracked();
            List<StatusFileDTO> untracked = state.getUntracked();
            String greenColor = "\u001B[32m";
            String redColorCode = "\u001B[31m";
            String resetColorCode = "\u001B[0m";
            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.FILES_READY_TO_BE_COMMITTED));

            for (int i = 0; i <tracked.size() ; i++){
                System.out.println(greenColor+ "\t" +tracked.get(i).getPath() + resetColorCode);
            }

            System.out.println("\n" + "\n");
            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.UNTRACKED_FILES) + "\n");

            for (int i = 0; i <untracked.size() ; i++){
                System.out.println(redColorCode+ "\t"+untracked.get(i).getPath() + resetColorCode);
            }

            System.out.println();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
