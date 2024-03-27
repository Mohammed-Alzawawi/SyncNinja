package org.syncninja.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Directory extends SyncNode {
    private String path;

    @Relationship(type = "MainBranch")
    private Branch branch;

    public Directory() {
    }

    public Directory(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    @Override
    public String toString() {
        return "Directory{" +
                "path='" + path + '\'' +
                ", branch=" + branch +
                '}';
    }
}
