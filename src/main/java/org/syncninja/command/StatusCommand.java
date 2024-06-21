package org.syncninja.command;

import org.syncninja.dto.CommitFileDTO;
import org.syncninja.dto.FileStatusEnum;
import org.syncninja.dto.StatusFileDTO;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;
import org.syncninja.service.StatusService;
import org.syncninja.util.FileTrackingState;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "status")
public class StatusCommand extends BaseCommand {
    private final StatusService statusService;
    private final StateTreeService stateTreeService;

    public StatusCommand() {
        this.statusService = new StatusService();
        this.stateTreeService = new StateTreeService();
    }

    @Override
    public void run() {
        try {
            String path = System.getProperty("user.dir");
            FileTrackingState state = statusService.getState(path);
            if (state == null) {
                throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_NOT_INITIALIZED, new Object[]{path}));
            }
            StateRoot stateRoot = stateTreeService.getStateRoot(path);
            printStatusMessage(state, stateRoot);
            Neo4jSession.closeSession();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String getStatusFileString(FileStatusEnum fileStatusEnum) {
        if (fileStatusEnum == FileStatusEnum.IS_DELETED) {
            return "deleted: ";
        } else if (fileStatusEnum == FileStatusEnum.IS_NEW) {
            return "new file: ";
        }
        return "modified: ";
    }

    private void printStatusMessage(FileTrackingState state, StateRoot stateRoot) {
        System.out.print(ResourceMessagingService.getMessage(ResourceBundleEnum.CURRENT_BRANCH, new Object[]{stateRoot.getCurrentBranch().getName()}) + "\n\n");
        Boolean conflict = statusService.hasConflict(stateRoot);

        List<CommitFileDTO> tracked = state.getTracked();
        List<StatusFileDTO> untracked = state.getUntracked();

        String greenColor = "\u001B[32m";
        String redColorCode = "\u001B[31m";
        String resetColorCode = "\u001B[0m";
        System.out.print(ResourceMessagingService.getMessage(ResourceBundleEnum.CHANGES_READY_TO_BE_COMMITTED) + "\n\n");

        for (CommitFileDTO commitFileDTO : tracked) {
            System.out.print(greenColor + "\t" + getStatusFileString(commitFileDTO.getCommitFile().getStatusEnum()) + " " + commitFileDTO.getRelativePath() + resetColorCode + "\n");
        }

        System.out.print("\n");
        if(conflict){
            System.out.print(ResourceMessagingService.getMessage(ResourceBundleEnum.CONFLICT_DETECTED) + "\n");
        }
        System.out.print(ResourceMessagingService.getMessage(ResourceBundleEnum.UNTRACKED_FILES) + "\n\n");

        for (StatusFileDTO statusFileDTO : untracked) {
            System.out.println(redColorCode + "\t" + getStatusFileString(statusFileDTO.getFileStatus()) + " " + statusFileDTO.getRelativePath() + resetColorCode);
        }
    }
}
