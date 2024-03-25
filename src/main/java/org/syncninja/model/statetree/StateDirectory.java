package org.syncninja.model.statetree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class StateDirectory extends StateNode {
    @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
    protected Set<StateNode> internalNodes = new HashSet<>();
    private long lastAccessed;

    public StateDirectory() {
    }

    public StateDirectory(String path) throws IOException {
        super(path);
        this.internalNodes = new HashSet<>();
        Path file = Paths.get(path);
        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
        this.lastAccessed = attrs.lastAccessTime().toMillis();

    }
    public long getLastAccessed() {
        return lastAccessed;
    }
    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public Set<StateNode> getInternalNodes() {
        return internalNodes;
    }

    public void setInternalNodes(Set<StateNode> internalNodes) {
        this.internalNodes = internalNodes;
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