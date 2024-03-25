package org.syncninja.model.statetree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NodeEntity
public class StateDirectory extends StateNode {
    @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
    protected List<StateNode> internalNodes = new ArrayList<>();

    public StateDirectory() {
    }

    public StateDirectory(String path) {
        super(path);
        this.internalNodes = new ArrayList<>();
    }

    public List<StateNode> getInternalNodes() {
        return internalNodes;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    public void addFile(StateNode internalFile) {
        this.internalNodes.add(internalFile);
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean isRoot() {
        return false;
    }
}