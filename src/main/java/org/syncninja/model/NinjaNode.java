package org.syncninja.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@NodeEntity
public abstract class NinjaNode extends SyncNode {

    @Relationship(type = "ParentOf")
    private Set<NinjaNode> childrenSet;

    public NinjaNode() {
    }

    public Set<NinjaNode> getChildrenSet() {
        return childrenSet;
    }

    public void setChildrenSet(Set<NinjaNode> childrenSet) {
        this.childrenSet = childrenSet;
    }

    public void addToChildrenSet(NinjaNode ninjaNode) {
        childrenSet.add(ninjaNode);
    }

    public void DeleteFromChildrenSet(NinjaNode ninjaNode) {
        childrenSet.remove(ninjaNode);
    }
}
