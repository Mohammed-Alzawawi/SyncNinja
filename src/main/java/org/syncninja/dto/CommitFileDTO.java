package org.syncninja.dto;

import org.syncninja.model.commitTree.CommitFile;

public class CommitFileDTO {
    private CommitFile commitFile;
    private String path;

    public CommitFileDTO() {
    }

    public CommitFileDTO(CommitFile commitFile, String path) {
        this.commitFile = commitFile;
        this.path = path;
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
}
