package org.syncninja.command;

import org.syncninja.Utilities.ResourceBundleEnum;
import org.syncninja.model.StateTree;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.service.ResourceMessagingService;
import picocli.CommandLine;

@CommandLine.Command(name = "status")
public class StatusCommand implements Runnable{
    private final ResourceMessagingService  resourceMessagingService =new ResourceMessagingService();
    @Override
    public void run() {
        StateTreeRepository stateTreeRepository = new StateTreeRepository();
        System.out.println(stateTreeRepository.findById( "C:\\Users\\user\\Desktop\\c++"));
    }
}
