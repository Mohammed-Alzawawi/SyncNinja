package org.syncninja.model.committree.mergetree;

import org.syncninja.model.committree.CommitFile;

public class MergeFile extends CommitFile {

    public MergeFile() {}

    public MergeFile(CommitFile commitFile) {
        this.setPath(commitFile.getPath());
        this.setStatusEnum(commitFile.getStatusEnum());
        this.setLineNumberList(commitFile.getLineNumberList());
        this.setNewValuesList(commitFile.getNewValuesList());
        this.setOldValuesList(commitFile.getOldValuesList());
    }
}
