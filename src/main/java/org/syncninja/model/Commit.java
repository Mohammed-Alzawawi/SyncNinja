package org.syncninja.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.syncninja.model.commitTree.CommitDirectory;

import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class Commit extends NinjaNode {
    private String message;
    private boolean isCommitted;
    @Relationship(type = "CommitTree")
    private CommitDirectory commitTree;

    public Commit() {

    }

    public Commit(String message) {
        this.message = message;
        isCommitted = false;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CommitDirectory getCommitTree() {
        return commitTree;
    }

    public void setCommitTree(CommitDirectory commitTree) {
        this.commitTree = commitTree;
    }

    public boolean isCommitted() {
        return isCommitted;
    }

    public void setCommitted(boolean isCommitted) {
        this.isCommitted = isCommitted;
    }
}