package org.syncninja.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.syncninja.model.commitTree.CommitDirectory;

import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class Commit extends NinjaNode {
    private String message;

    @Relationship(type = "CommitTree")
    private List<CommitDirectory> commitTree;

    public Commit() {
        commitTree = new ArrayList<>();
    }

    public Commit(String message) {
        commitTree = new ArrayList<>();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<CommitDirectory> getCommitTree() {
        return commitTree;
    }

    public void setCommitTree(List<CommitDirectory> commitTree) {
        this.commitTree = commitTree;
    }
}