package org.syncninja.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class StateDirectory extends StateTree {
    @Relationship(type = "HAS" , direction = Relationship.Direction.OUTGOING)
    private Set<StateTree> internalNodes = new HashSet<>();

    public StateDirectory() {;
    }

    public StateDirectory(String path) {
        super(path);
        this.internalNodes = new HashSet<>();
    }

    public Set<StateTree> getInternalNodes() {
        return internalNodes;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    public void addFile(StateTree internalFile) {
        this.internalNodes.add(internalFile);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setInternalNodes(Set<StateTree> internalNodes) {
        this.internalNodes = internalNodes;
    }
}
