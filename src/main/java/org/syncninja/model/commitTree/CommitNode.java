package org.syncninja.model.commitTree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.syncninja.model.SyncNode;

@NodeEntity
public abstract class CommitNode extends SyncNode {
    private String path;

    public CommitNode(String path) {
        this.path = path;
    }

    public CommitNode() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}