package org.syncninja.model.commitTree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class CommitDirectory extends CommitNode {
    @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
    private List<CommitNode> commitNodeList = new ArrayList<>();

    public CommitDirectory() {

    }

    public CommitDirectory(String path) {
        super(path);
    }

    public List<CommitNode> getCommitNodeList() {
        return commitNodeList;
    }

    public void setCommitNodeList(List<CommitNode> commitNodeList) {
        this.commitNodeList = commitNodeList;
    }

    public void addNode(CommitNode commitNode) {
        commitNodeList.add(commitNode);
    }

}