package org.syncninja.model.statetree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.syncninja.util.Fetcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class StateFile extends StateNode {
    private List<String> lines;
    private long lastModified;

    public StateFile(String path) throws IOException {
        super(path);
        lines = Fetcher.readFile(path);
        File file = new File(path);
        this.lastModified = file.lastModified();
    }

    public StateFile(String path, List<String> lines) throws IOException {
        super(path);
        this.lines = lines;
    }

    public StateFile() {
        lines = new ArrayList<>();
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