package org.syncninja.command;

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
public class StatusCommand extends BaseCommand {
    private final StatusService statusService;

    public StatusCommand() {
        this.statusService = new StatusService();
    }

    @Override
    public void run() {
        try {
            String path = System.getProperty("user.dir");
            FileTrackingState state = statusService.getState(path);
            if (state == null) {
                throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path}));
            }
            printStatusMessage(state);
            Neo4jSession.closeSession();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void printStatusMessage(FileTrackingState state) {
        List<CommitFileDTO> tracked = state.getTracked();
        List<StatusFileDTO> untracked = state.getUntracked();


        String greenColor = "\u001B[32m";
        String redColorCode = "\u001B[31m";
        String resetColorCode = "\u001B[0m";
        System.out.print(ResourceMessagingService.getMessage(ResourceBundleEnum.CHANGES_READY_TO_BE_COMMITTED) + "\n\n");

        for (CommitFileDTO commitFileDTO : tracked) {
            System.out.print(greenColor + "\t" + commitFileDTO.getCommitFile().getStatusEnum() + " " + commitFileDTO.getRelativePath() + resetColorCode + "\n");
        }

        System.out.print("\n");
        System.out.print(ResourceMessagingService.getMessage(ResourceBundleEnum.UNTRACKED_FILES) + "\n\n");

        for (StatusFileDTO statusFileDTO : untracked) {
            System.out.println(redColorCode + "\t" + statusFileDTO.getFileStatus() + " " + statusFileDTO.getRelativePath() + resetColorCode);
        }
    }

}
