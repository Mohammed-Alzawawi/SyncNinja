package org.syncninja.model.committree.mergetree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.syncninja.model.Commit;

@NodeEntity
public class MergeCommit extends Commit {
    @Relationship(type = "referenceTo")
    private Commit referenceCommit;

    public MergeCommit() {

    }

    public MergeCommit(Commit commit) {
        this.setCommitted(commit.isCommitted());
        this.setMessage(commit.getMessage());
        this.setReferenceCommit(commit);
    }

    public void setReferenceCommit(Commit referenceCommit) {
        this.referenceCommit = referenceCommit;
    }

    public Commit getReferenceCommit() {
        return referenceCommit;
    }
}
