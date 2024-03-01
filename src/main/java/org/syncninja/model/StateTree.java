package org.syncninja.model;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.io.File;
@NodeEntity
public abstract class StateTree {
    @Id
    protected String path;
    protected long lastModified;

    public StateTree() {

    }

    public StateTree(String path) {
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

    public String getPath(){
        return path;
    }
    public abstract boolean isDirectory();
}
