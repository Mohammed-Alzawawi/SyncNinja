package org.syncninja.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.syncninja.model.commitTree.CommitDirectory;

import java.util.List;
import java.util.Set;

@NodeEntity
public abstract class NinjaNode extends SyncNode{

    @Relationship(type = "ParentOf")
    private List<Branch> childrenSet;

    @Relationship(type = "nextCommit")
    private Commit commit;

    public NinjaNode() {}

    public List<Branch> getChildrenSet() {
        return childrenSet;
    }
}