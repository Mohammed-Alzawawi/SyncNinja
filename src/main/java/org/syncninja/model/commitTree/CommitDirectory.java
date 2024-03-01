package org.syncninja.model.commitTree;

import java.util.ArrayList;
import java.util.List;

public class CommitDirectory extends CommitNode {
    private List<CommitNode> commitNodeList = new ArrayList<>();

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

    public void removeNode(CommitNode commitNode) {
        commitNodeList.remove(commitNode);
    }
}
