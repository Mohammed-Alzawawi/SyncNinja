package org.syncninja.util;

import org.syncninja.dto.StatusFileDTO;

import java.util.ArrayList;
import java.util.List;

public class FileState {
    private final List<StatusFileDTO> untracked;
    private final List<StatusFileDTO> tracked;

    public FileState() {
        this.untracked = new ArrayList<>();
        this.tracked = new ArrayList<>();
    }

    public List<StatusFileDTO> getUntracked() {
        return untracked;
    }

    public List<StatusFileDTO> getTracked() {
        return tracked;
    }

    public StatusFileDTO getUntrackedDTO(String path) {
        for (StatusFileDTO statusFileDTO : untracked) {
            if (statusFileDTO.getPath().equals(path)) {
                return statusFileDTO;
            }
        }
        return null;
    }
}
