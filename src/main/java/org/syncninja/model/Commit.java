package org.syncninja.model;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Commit extends NinjaNode {
    private String message;

    public Commit() {
    }

    public Commit(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
