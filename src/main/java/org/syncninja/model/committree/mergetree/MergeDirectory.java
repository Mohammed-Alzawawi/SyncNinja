package org.syncninja.model.committree.mergetree;

import org.syncninja.model.committree.CommitDirectory;

import java.util.ArrayList;

public class MergeDirectory extends CommitDirectory {
    public MergeDirectory(){

    }
    public MergeDirectory(CommitDirectory commitDirectory) {
        this.setPath(commitDirectory.getPath());
        this.setCommitNodeList(new ArrayList<>());
        this.setStatusEnum(commitDirectory.getStatusEnum());
    }
}
