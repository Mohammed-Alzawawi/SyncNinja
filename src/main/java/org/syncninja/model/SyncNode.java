package org.syncninja.model;

import org.neo4j.ogm.annotation.Id;

import java.util.UUID;

public abstract class SyncNode {

    @Id
    private final String id;

    public SyncNode() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }
}
