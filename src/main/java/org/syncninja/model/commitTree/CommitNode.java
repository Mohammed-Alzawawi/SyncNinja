package org.syncninja.model.commitTree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.syncninja.model.SyncNode;

@NodeEntity
public abstract class CommitNode extends SyncNode {
    private String path;
    private boolean isDirectory;

    public CommitNode(String path, boolean isDirectory) {
        this.path = path;
        this.isDirectory = isDirectory;
    }

    public CommitNode() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
}