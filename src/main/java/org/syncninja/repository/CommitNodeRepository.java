package org.syncninja.repository;

import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.util.Neo4jSession;

import java.util.Collection;
import java.util.Optional;

public class CommitNodeRepository {
    public void save(CommitNode commitNode) {
        Session session = Neo4jSession.getSession();
        session.save(commitNode);
    }

    public Optional<CommitNode> findByPath(String path) {
        Session session = Neo4jSession.getSession();
        Filter filter = new Filter("path", ComparisonOperator.EQUALS, path);
        Collection<CommitNode> commitNodes = session.loadAll(CommitNode.class, filter, -1);
        return Optional.ofNullable((commitNodes.isEmpty()) ? null : commitNodes.iterator().next());
    }
}

