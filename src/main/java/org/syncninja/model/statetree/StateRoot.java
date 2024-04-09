package org.syncninja.model.statetree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.syncninja.model.Branch;
import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import java.io.IOException;

@NodeEntity
public class StateRoot extends StateDirectory {
    @Relationship(type = "CURRENT_BRANCH", direction = Relationship.Direction.OUTGOING)
    private Branch currentBranch;
    @Relationship(type = "CURRENT_COMMIT", direction = Relationship.Direction.OUTGOING)
    private Commit currentCommit;

    public StateRoot() {
    }

    public StateRoot(String path) throws IOException {
        super(path);
    }

    public StateRoot(String path, Branch currentBranch) throws IOException {
        super(path);
        this.currentBranch = currentBranch;
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

    public NinjaNode getCurrentNinjaNode(){
        if(currentCommit == null){
            return currentBranch;
        }
        return currentCommit;
    }

    @Override
    public boolean isRoot() {
        return true;
    }
}