package org.syncninja.model.committree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.syncninja.dto.FileStatusEnum;

import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class CommitDirectory extends CommitNode {
    @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
    private List<CommitNode> commitNodeList = new ArrayList<>();

    public CommitDirectory() {}

    public CommitDirectory(String path, FileStatusEnum fileStatusEnum) {
        super(path);
        this.commitNodeList = new ArrayList<>();
        this.setStatusEnum(fileStatusEnum);
    }

    public List<CommitNode> getCommitNodeList() {
        return this.commitNodeList;
    }

    public void setCommitNodeList(List<CommitNode> commitNodeList) {
        this.commitNodeList = commitNodeList;
    }

    public void addNode(CommitNode commitNode) {
        this.commitNodeList.add(commitNode);
    }
}
