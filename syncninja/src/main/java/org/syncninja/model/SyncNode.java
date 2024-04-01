package org.syncninja.model;

import org.neo4j.ogm.annotation.Id;

import java.util.Date;
import java.util.UUID;

public abstract class SyncNode {

    @Id
    private final String id;
    private final Date creationTime;

    public SyncNode() {
        this.id = UUID.randomUUID().toString();
        creationTime = new Date();
    }

    public String getId() {
        return id;
    }

    public Date getCreationTime() {
        return creationTime;
    }
}