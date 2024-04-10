package org.syncninja.model.committree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.syncninja.dto.FileStatusEnum;
import org.syncninja.util.LinesContainer;

import java.util.List;

@NodeEntity
public class CommitFile extends CommitNode {

    private List<Integer> lineNumberList;
    private List<String> newValuesList;
    private List<String> oldValuesList;

    public CommitFile() {}

    public CommitFile(String path, FileStatusEnum fileStatusEnum, List<Integer> lineNumberList, List<String> newValuesList, List<String> oldValuesList) {
        super(path);
        this.lineNumberList = lineNumberList;
        this.newValuesList = newValuesList;
        this.oldValuesList = oldValuesList;
        this.setStatusEnum(fileStatusEnum);
    }

    public CommitFile(String path) {
        super(path);
    }

    public List<Integer> getLineNumberList() {
        return lineNumberList;
    }

    public void setLineNumberList(List<Integer> lineNumberList) {
        this.lineNumberList = lineNumberList;
    }

    public List<String> getNewValuesList() {
        return newValuesList;
    }

    public void setNewValuesList(List<String> newValuesList) {
        this.newValuesList = newValuesList;
    }

    public List<String> getOldValuesList() {
        return oldValuesList;
    }

    public void setOldValuesList(List<String> oldValuesList) {
        this.oldValuesList = oldValuesList;
    }

    public void updateCommitList(LinesContainer linesContainer) {
        this.lineNumberList = linesContainer.getLineNumbers();
        this.newValuesList = linesContainer.getNewLines();
        this.oldValuesList = linesContainer.getOldLines();
    }
}