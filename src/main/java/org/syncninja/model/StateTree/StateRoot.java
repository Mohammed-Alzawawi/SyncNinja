package org.syncninja.model.StateTree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.syncninja.model.Branch;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitNode;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class StateRoot extends StateDirectory{
    @Relationship(type="CURRENT_BRANCH", direction = Relationship.Direction.OUTGOING)
    private Branch currentBranch;
    @Relationship(type="CURRENT_COMMIT", direction = Relationship.Direction.OUTGOING)
    private CommitNode currentCommit;

    public StateRoot() {
    }

    public StateRoot(String path) {
        super(path);
    }

    public StateRoot(String path, Branch currentBranch) {
        super(path);
        this.currentBranch = currentBranch;
    }

    public Branch getCurrentBranch() {
        return currentBranch;
    }
    public void setCurrentBranch(Branch currentBranch) {
        this.currentBranch = currentBranch;
    }

    public CommitNode getCurrentCommit() {
        return currentCommit;
    }

    public void setCurrentCommit(CommitNode currentCommit) {
        this.currentCommit = currentCommit;
    }
    @Override
    public boolean isRoot() {
        return true;
    }
}
