package org.syncninja.util;

import java.util.ArrayList;
import java.util.List;

public class FileState {
    private List<String> untracked;
    private List<String> tracked;

    public FileState() {
        this.untracked = new ArrayList<>();
        this.tracked = new ArrayList<>();
    }

    public List<String> getUntracked() {
        return untracked;
    }

    public List<String> getTracked() {
        return tracked;
    }
}
