package org.syncninja.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.syncninja.Utilities.Fetcher;
import org.syncninja.Utilities.SHA256;


import java.io.IOException;
import java.util.List;
@NodeEntity
public class StateFile extends StateTree {
    private List<String> lines;

    String hashValue;
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

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public String getPath() {
        return path;
    }

    public List<String> getLines() {
        return lines;
    }
    @Override
    public boolean isDirectory() {
        return false;
    }
    public String getHashValue() {
        return hashValue;
    }
}
