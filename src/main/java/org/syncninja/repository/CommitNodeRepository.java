package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.util.Neo4jSession;

import java.util.Collections;
import java.util.Optional;

public class CommitNodeRepository {
    public void save(CommitNode commitNode) {
        Session session = Neo4jSession.getSession();
        session.save(commitNode);
    }
    public Optional<CommitNode> findById(String path){
        Session session = Neo4jSession.getSession();
        CommitNode commitNode = session.load(CommitNode.class , path);
        return Optional.ofNullable(commitNode);
    }

    public Optional<CommitNode> findByPath(String path){
        Session session = Neo4jSession.getSession();
        CommitNode commitNode = session.queryForObject(CommitNode.class, "MATCH(c:CommitNode) WHERE c.path = $path RETURN c", Collections.singletonMap("path", path));
        return Optional.ofNullable(commitNode);
    }


}
