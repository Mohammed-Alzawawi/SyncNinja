package org.syncninja.dto;

import org.syncninja.model.statetree.StateFile;

public class StatusFileDTO {
    private StateFile stateFile;
    private String path;
    private String relativePath;
    private FileStatusEnum fileStatus;

    public StatusFileDTO(FileStatusEnum fileStatus, StateFile stateFile, String path, String relativePath) {
        this.relativePath = relativePath;
        this.fileStatus = fileStatus;
        this.stateFile = stateFile;
        this.path = path;
    }

    public FileStatusEnum getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(FileStatusEnum fileStatus) {
        this.fileStatus = fileStatus;
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
