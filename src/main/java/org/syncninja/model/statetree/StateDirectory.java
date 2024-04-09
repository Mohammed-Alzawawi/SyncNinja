package org.syncninja.model.statetree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class StateDirectory extends StateNode {

    @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
    protected List<StateNode> internalNodes = new ArrayList<>();
    private long lastAccessed;

    public StateDirectory() {
    }

    public StateDirectory(String path) throws IOException {
        super(path);
        this.internalNodes = new ArrayList<>();
        Path file = Paths.get(path);
        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
        this.lastAccessed = attrs.lastAccessTime().toMillis();
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public List<StateNode> getInternalNodes() {
        return internalNodes;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    public void addFile(StateNode internalFile) {
        this.internalNodes.add(internalFile);
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean isRoot() {
        return false;
    }
}