package org.syncninja.model.StateTree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.syncninja.util.Fetcher;
import org.syncninja.util.SHA256;

import java.io.IOException;
import java.util.List;

@NodeEntity
public class StateFile extends StateTree {
    private String hashValue;
    private List<String> lines;

    public StateFile(String path) throws IOException {
        super(path);
        hashValue = SHA256.hashValue(path);
        lines = Fetcher.readFile(path);
    }

    public StateFile() {
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    @Override
    public boolean isRoot() {
        return false;
    }
}
