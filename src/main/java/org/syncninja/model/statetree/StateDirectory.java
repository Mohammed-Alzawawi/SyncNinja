package org.syncninja.model.statetree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class StateDirectory extends StateNode {
    @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
    protected Set<StateNode> internalNodes = new HashSet<>();

    public StateDirectory() {
    }

    public StateDirectory(String path) {
        super(path);
        this.internalNodes = new HashSet<>();
    }

    public Set<StateNode> getInternalNodes() {
        return internalNodes;
    }

    public void setInternalNodes(Set<StateNode> internalNodes) {
        this.internalNodes = internalNodes;
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