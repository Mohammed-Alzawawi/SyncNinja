package org.syncninja.util;

import org.syncninja.dto.CommitFileDTO;
import org.syncninja.dto.FileStatusEnum;
import org.syncninja.dto.StatusFileDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileTrackingState {
    private final List<StatusFileDTO> untracked;
    private final List<CommitFileDTO> tracked;
    private final Map<String, FileStatusEnum> directoriesState;

    public FileTrackingState() {
        this.untracked = new ArrayList<>();
        this.tracked = new ArrayList<>();
        this.directoriesState = new HashMap<>();
    }

    public Map<String, FileStatusEnum> getDirectoriesState() {
        return directoriesState;
    }

    public List<StatusFileDTO> getUntracked() {
        return untracked;
    }

    public List<CommitFileDTO> getTracked() {
        return tracked;
    }
}