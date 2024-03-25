package org.syncninja.model.statetree;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.io.File;

@NodeEntity
public abstract class StateNode {
    @Id
    protected String path;
    public StateNode() {
    }

    public StateNode(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public abstract boolean isDirectory();

    public abstract boolean isRoot();
}