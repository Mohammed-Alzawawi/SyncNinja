package org.syncninja.model.StateTree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class StateDirectory extends StateTree {
    @Relationship(type = "HAS" , direction = Relationship.Direction.OUTGOING)
    private Set<StateTree> internalNodes;

    public StateDirectory() {;
    }

    public StateDirectory(String path) {
        super(path);
        this.internalNodes = new HashSet<>();
    }

    public String getPath() {
        return path;
    }

    public Set<StateTree> getInternalNodes() {
        return internalNodes;
    }


    @Override
    public boolean isDirectory() {
        return true;
    }
    public void addfile(StateTree internalfile) {

        this.internalNodes.add(internalfile);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setInternalNodes(Set<StateTree> internalNodes) {
        this.internalNodes = internalNodes;
    }

    @Override
    public boolean isRoot() {
        return false;
    }
}
