package org.syncninja.util;

import org.syncninja.dto.CommitFileDTO;
import org.syncninja.dto.StatusFileDTO;
import org.syncninja.repository.CommitNodeRepository;

import java.util.ArrayList;
import java.util.List;

public class FileTrackingState {
    private final List<StatusFileDTO> untracked;
    private final List<CommitFileDTO> tracked;


    public FileTrackingState() {
        this.untracked = new ArrayList<>();
        this.tracked = new ArrayList<>();
    }

    public List<StatusFileDTO> getUntracked() {
        return untracked;
    }

    public List<CommitFileDTO> getTracked() {
        return tracked;
    }
}