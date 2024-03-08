package org.syncninja.model.StateTree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.syncninja.model.Branch;
import org.syncninja.model.Commit;

@NodeEntity
public class StateRoot extends StateDirectory {
    @Relationship(type = "CURRENT_BRANCH", direction = Relationship.Direction.OUTGOING)
    private Branch currentBranch;
    @Relationship(type = "CURRENT_COMMIT", direction = Relationship.Direction.OUTGOING)
    private Commit currentCommit;

    public StateRoot() {
    }

    public StateRoot(String path) {
        super(path);
    }

    public StateRoot(String path, Branch currentBranch, Commit currentCommit) {
        super(path);
        this.currentBranch = currentBranch;
        this.currentCommit = currentCommit;
    }

    public Branch getCurrentBranch() {
        return currentBranch;
    }

    public void setCurrentBranch(Branch currentBranch) {
        this.currentBranch = currentBranch;
    }

    public Commit getCurrentCommit() {
        return currentCommit;
    }

    public void setCurrentCommit(Commit currentCommit) {
        this.currentCommit = currentCommit;
    }

    @Override
    public boolean isRoot() {
        return true;
    }
}