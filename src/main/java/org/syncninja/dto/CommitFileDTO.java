package org.syncninja.dto;

import org.syncninja.model.committree.CommitFile;

public class CommitFileDTO {
    private CommitFile commitFile;
    private String path;
    private String relativePath;

    public CommitFileDTO() {
    }

    public CommitFileDTO(CommitFile commitFile, String path, String relativePath) {
        this.commitFile = commitFile;
        this.path = path;
        this.relativePath = relativePath;
    }

    public CommitFile getCommitFile() {
        return commitFile;
    }

    public void setCommitFile(CommitFile commitFile) {
        this.commitFile = commitFile;
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