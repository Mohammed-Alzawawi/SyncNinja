package org.syncninja.model.statetree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.syncninja.util.Fetcher;
import org.syncninja.util.SHA256;

import java.io.File;
import java.io.IOException;
import java.util.List;

@NodeEntity
public class StateFile extends StateNode {
    private String hashValue;
    private List<String> lines;
    private long lastModified;

    public StateFile(String path) throws IOException {
        super(path);
        hashValue = SHA256.hashValue(path);
        lines = Fetcher.readFile(path);
        File file = new File(path);
        this.lastModified = file.lastModified();
    }

    public StateFile(String path, List<String> lines) throws IOException {
        super(path);
        hashValue = SHA256.hashValue(lines);
        this.lines = lines;
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

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public boolean isRoot() {
        return false;
    }
}