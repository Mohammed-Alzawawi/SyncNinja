package org.syncninja.model.committree.mergetree;

import org.syncninja.model.committree.CommitFile;

import java.util.List;

public class MergeFile extends CommitFile {
    private List<Integer> conflictLineNumber;
    private List<String> conflictLine;
    private List<String> conflictSolution;

    public MergeFile(CommitFile commitFile) {
        this.setPath(commitFile.getPath());
        this.setStatusEnum(commitFile.getStatusEnum());
        this.setLineNumberList(commitFile.getLineNumberList());
        this.setNewValuesList(commitFile.getNewValuesList());
        this.setOldValuesList(commitFile.getOldValuesList());
    }
}
