package org.syncninja.repository;
import org.neo4j.ogm.session.Session;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.util.Neo4jSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

public class CommitNodeRepository {
    public void save(CommitNode commitNode){
        Session session = Neo4jSession.getSession();
        session.save(commitNode);
    }

    public Optional<CommitDirectory> getCommitNodeRoot(String path){
        Session session = Neo4jSession.getSession();
        CommitDirectory commitDirectory = session.queryForObject(CommitDirectory.class, "MATCH (c:CommitDirectory) WHERE c.path = $path AND c.isCommitted = false RETURN c", Collections.singletonMap("path", path));
        return Optional.ofNullable(commitDirectory);
    }
}
