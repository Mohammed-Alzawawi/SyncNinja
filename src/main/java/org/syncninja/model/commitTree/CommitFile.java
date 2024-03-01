package org.syncninja.model.commitTree;

import java.util.List;

public class CommitFile extends CommitNode {
    private List<Integer> lineNumberList;
    private List<String> newValuesList;
    private List<String> oldValuesList;

    public CommitFile(String path, List<Integer> lineNumberList, List<String> newValuesList, List<String> oldValuesList) {
        super(path);
        this.lineNumberList = lineNumberList;
        this.newValuesList = newValuesList;
        this.oldValuesList = oldValuesList;
    }

    public CommitFile(String path) {
        super(path);
    }

    // Getters and setters for lineNumberList, newValuesList, and oldValuesList
}
