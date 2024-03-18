package org.syncninja.dto;

import org.syncninja.model.statetree.StateFile;

public class StatusFileDTO {
    private boolean isNew;
    private StateFile stateFile;
    private String path;
    private String relativePath;

    public StatusFileDTO(boolean isNew, StateFile stateFile, String path, String relativePath) {
        this.relativePath = relativePath;
        this.isNew = isNew;
        this.stateFile = stateFile;
        this.path = path;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public StateFile getStateFile() {
        return stateFile;
    }

    public void setStateFile(StateFile stateFile) {
        this.stateFile = stateFile;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }
}