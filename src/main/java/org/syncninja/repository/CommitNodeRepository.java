package org.syncninja.repository;
import org.neo4j.ogm.session.Session;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.SyncNode;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.util.Neo4jSession;

import java.util.Optional;

public class CommitNodeRepository {
    public void save(CommitNode commitNode){
        Session session = Neo4jSession.getSession();
        session.save(commitNode);
    }


}
