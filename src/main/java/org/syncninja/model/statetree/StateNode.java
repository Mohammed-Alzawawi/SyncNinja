package org.syncninja.model.statetree;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Objects;

@NodeEntity
public abstract class StateNode {
    @Id
    protected String path;
    public StateNode() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateNode stateNode = (StateNode) o;
        return Objects.equals(path, stateNode.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
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