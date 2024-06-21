package org.syncninja.model;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Branch extends NinjaNode {
    private String name;
    private Commit lastCommit;

    public Branch() {}

    public Branch(String name) {
        super();
        this.name = name;
    }

    public Commit getLastCommit() {
        return lastCommit;
    }

    public NinjaNode getLastNinjaNode() {
        if(lastCommit == null){
            return this;
        }
        return lastCommit;
    }

    public void setLastCommit(Commit lastCommit) {
        this.lastCommit = lastCommit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasLastCommit(){
        return lastCommit != null;
    }
}
