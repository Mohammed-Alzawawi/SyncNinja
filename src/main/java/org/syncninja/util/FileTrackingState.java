package org.syncninja.util;

import org.syncninja.dto.StatusFileDTO;

import java.util.ArrayList;
import java.util.List;

public class FileTrackingState {
    private final List<StatusFileDTO> untracked;
    private final List<StatusFileDTO> tracked;

    public FileTrackingState() {
        this.untracked = new ArrayList<>();
        this.tracked = new ArrayList<>();
    }

    public List<StatusFileDTO> getUntracked() {
        return untracked;
    }

    public List<StatusFileDTO> getTracked() {
        return tracked;
    }
}