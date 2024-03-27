package org.syncninja.command;

import org.syncninja.OutputCollector;
import org.syncninja.dto.CommitFileDTO;
import org.syncninja.dto.StatusFileDTO;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StatusService;
import org.syncninja.util.FileTrackingState;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "status")
public class StatusCommand extends CommonOptions implements Runnable {
    private final StatusService statusService;

    public StatusCommand() {
        this.statusService = new StatusService();
    }

    @Override
    public void run() {
        String path = this.directory;
        try {
            FileTrackingState state = statusService.getState(path);
            if (state == null) {
                throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path}));
            }
            printStatus(state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void printStatus(FileTrackingState state) {
        List<CommitFileDTO> tracked = state.getTracked();
        List<StatusFileDTO> untracked = state.getUntracked();

        String greenColor = "\u001B[32m";
        String redColorCode = "\u001B[31m";
        String resetColorCode = "\u001B[0m";
        OutputCollector.addString(sessionId, ResourceMessagingService.getMessage(ResourceBundleEnum.FILES_READY_TO_BE_COMMITTED));

        for (int i = 0; i < tracked.size(); i++) {
            OutputCollector.addString(sessionId, greenColor + "\t" + tracked.get(i).getRelativePath() + resetColorCode);
        }

        System.out.println("\n" + "\n");
        OutputCollector.addString(sessionId, ResourceMessagingService.getMessage(ResourceBundleEnum.UNTRACKED_FILES) + "\n");

        for (int i = 0; i < untracked.size(); i++) {
            OutputCollector.addString(sessionId, redColorCode + "\t" + untracked.get(i).getRelativePath() + resetColorCode);
        }

        OutputCollector.addString(sessionId, "");
    }
}