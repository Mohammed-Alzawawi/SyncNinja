package org.syncninja.dto;


import org.syncninja.model.StateTree.StateFile;

public class StatusFileDTO {
    private boolean isNew;
    private StateFile stateFile;
    private String path;

    public StatusFileDTO(boolean isNew, StateFile stateFile, String path) {
        this.isNew = isNew;
        this.stateFile = stateFile;
        this.path = path;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
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
}
