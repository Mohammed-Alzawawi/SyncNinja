package org.syncninja.controller;

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

    public String run(String path) {
        try {
            FileTrackingState state = statusService.getState(path);
            if (state == null) {
                throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path}));
            }
            String response = getStatusMessage(state);
            Neo4jSession.closeSession();
            return response;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    private String getStatusMessage(FileTrackingState state) {
        List<CommitFileDTO> tracked = state.getTracked();
        List<StatusFileDTO> untracked = state.getUntracked();

        StringBuilder response = new StringBuilder();

        String greenColor = "\u001B[32m";
        String redColorCode = "\u001B[31m";
        String resetColorCode = "\u001B[0m";
        response.append(ResourceMessagingService.getMessage(ResourceBundleEnum.CHANGES_READY_TO_BE_COMMITTED)).append("\n\n");

        for (CommitFileDTO commitFileDTO : tracked) {
            response.append(greenColor).append("\t").append(commitFileDTO.getRelativePath()).append(resetColorCode).append("\n");
        }

        response.append("\n");
        response.append(ResourceMessagingService.getMessage(ResourceBundleEnum.UNTRACKED_FILES)).append("\n\n");

        for (StatusFileDTO statusFileDTO : untracked) {
            response.append(redColorCode).append("\t").append(statusFileDTO.getRelativePath()).append(resetColorCode).append("\n");
        }

        return response.toString();
    }
}