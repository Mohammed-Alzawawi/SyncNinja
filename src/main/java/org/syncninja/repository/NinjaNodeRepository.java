package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.NinjaNode;
import org.syncninja.util.Neo4jSession;

public class NinjaNodeRepository {
    public void save(NinjaNode ninjaNode) {
        Session session = Neo4jSession.getSession();
        session.save(ninjaNode);
    }
}
