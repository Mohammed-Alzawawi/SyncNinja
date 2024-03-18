package org.syncninja.model.statetree;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.io.File;

@NodeEntity
public abstract class StateNode {
    @Id
    protected String path;
    protected long lastModified;

    public StateNode() {
    }

    public StateNode(String path) {
        this.path = path;
        File file = new File(path);
        this.lastModified = file.lastModified();
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getPath() {
        return path;
    }

    public abstract boolean isDirectory();

    public abstract boolean isRoot();
}