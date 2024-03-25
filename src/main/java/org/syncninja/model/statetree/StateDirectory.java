package org.syncninja.model.statetree;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

<<<<<<< HEAD
<<<<<<< HEAD
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
=======
import java.util.ArrayList;
import java.util.List;
>>>>>>> 378bbd4 (add restore command)
=======
import java.util.ArrayList;
import java.util.List;
>>>>>>> 378bbd427e9de2384550df949268ee3d4974fad5
import java.util.Set;

@NodeEntity
public class StateDirectory extends StateNode {
    @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
<<<<<<< HEAD
<<<<<<< HEAD
    protected Set<StateNode> internalNodes = new HashSet<>();
    private long lastAccessed;
=======
    protected List<StateNode> internalNodes = new ArrayList<>();
>>>>>>> 378bbd4 (add restore command)
=======
    protected List<StateNode> internalNodes = new ArrayList<>();
>>>>>>> 378bbd427e9de2384550df949268ee3d4974fad5

    public StateDirectory() {
    }

    public StateDirectory(String path) throws IOException {
        super(path);
<<<<<<< HEAD
<<<<<<< HEAD
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
=======
        this.internalNodes = new ArrayList<>();
>>>>>>> 378bbd4 (add restore command)
=======
        this.internalNodes = new ArrayList<>();
>>>>>>> 378bbd427e9de2384550df949268ee3d4974fad5
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