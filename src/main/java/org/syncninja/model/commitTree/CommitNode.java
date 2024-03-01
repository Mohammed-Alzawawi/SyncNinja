package org.syncninja.model.commitTree;

import org.syncninja.model.SyncNode;

public abstract class CommitNode extends SyncNode {
    private String path;

    public CommitNode(String path) {
        this.path = path;
    }

    public CommitNode() {}

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}