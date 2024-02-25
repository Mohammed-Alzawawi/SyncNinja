package org.syncninja.model;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;
import java.util.UUID;

@NodeEntity
public abstract class NinjaNode extends SyncNode{
    @Id
    private final UUID id;

    @Relationship(type = "ParentOf")
    Set<NinjaNode> childrenSet;

    public NinjaNode() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public Set<NinjaNode> getChildrenSet() {
        return childrenSet;
    }

    public void setChildrenSet(Set<NinjaNode> childrenSet) {
        this.childrenSet = childrenSet;
    }

    public void addToChildrenSet(NinjaNode ninjaNode){
        childrenSet.add(ninjaNode);
    }

    public void DeleteFromChildrenSet(NinjaNode ninjaNode){
        childrenSet.remove(ninjaNode);
    }
}
