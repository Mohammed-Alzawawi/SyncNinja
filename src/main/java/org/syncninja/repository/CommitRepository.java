package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.Commit;
import org.syncninja.util.Neo4jSession;

public class CommitRepository {
    public Commit save(Commit commit){
        Session session = Neo4jSession.getSession();
        session.save(commit);
        return commit;
    }
}
