package org.syncninja.controller;

import org.syncninja.util.OutputCollector;
import org.syncninja.dto.CommitFileDTO;
import org.syncninja.dto.StatusFileDTO;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StatusService;
import org.syncninja.util.FileTrackingState;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "status")
public class StatusController {
    private final StatusService statusService;

    public StatusController() {
        this.statusService = new StatusService();
    }

    public void run(String path) {
        try {
            FileTrackingState state = statusService.getState(path);
            if (state == null) {
                throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path}));
            }
            printStatus(state);
            Neo4jSession.closeSession();
        } catch (Exception e) {
            OutputCollector.addString(e.getMessage());
        }
    }
    private void printStatus(FileTrackingState state) {
        List<CommitFileDTO> tracked = state.getTracked();
        List<StatusFileDTO> untracked = state.getUntracked();

        String greenColor = "\u001B[32m";
        String redColorCode = "\u001B[31m";
        String resetColorCode = "\u001B[0m";
        OutputCollector.addString(ResourceMessagingService.getMessage(ResourceBundleEnum.CHANGES_READY_TO_BE_COMMITTED) + "\n\n");

        for (int i = 0; i < tracked.size(); i++) {
            OutputCollector.addString(greenColor + "\t" + tracked.get(i).getRelativePath() + resetColorCode + "\n");
        }

        OutputCollector.addString("\n");
        OutputCollector.addString(ResourceMessagingService.getMessage(ResourceBundleEnum.UNTRACKED_FILES) + "\n\n");

        for (int i = 0; i < untracked.size(); i++) {
            OutputCollector.addString(redColorCode + "\t" + untracked.get(i).getRelativePath() + resetColorCode + "\n");
        }

        OutputCollector.addString("");
    }
}