package org.syncninja.model.committree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.syncninja.dto.FileStatusEnum;
import org.syncninja.model.SyncNode;

@NodeEntity
public abstract class CommitNode extends SyncNode {
    private FileStatusEnum statusEnum;
    private String path;
    private CommitDirectory parent;

    public CommitNode(String path) {
        this.path = path;
    }

    public CommitNode() {
    }

    public String getPath() {
        return path;
    }

    public FileStatusEnum getStatusEnum() {
        return statusEnum;
    }

    public void setStatusEnum(FileStatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public CommitDirectory getParent() {
        return parent;
    }
}