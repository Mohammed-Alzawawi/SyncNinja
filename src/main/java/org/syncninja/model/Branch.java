package org.syncninja.model;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Branch extends NinjaNode {
    private String name;

    public Branch() {
    }

    public Branch(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}