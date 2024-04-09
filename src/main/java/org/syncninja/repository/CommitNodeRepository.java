package org.syncninja.repository;

import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;
import org.syncninja.model.committree.CommitNode;
import org.syncninja.util.Neo4jSession;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class CommitNodeRepository {
    public void save(CommitNode commitNode) {
        Session session = Neo4jSession.getSession();
        session.save(commitNode);
    }

    public void delete(CommitNode commitNode) {
        Session session = Neo4jSession.getSession();
        session.query("MATCH (n:CommitNode)-[*]->(child:CommitNode) WHERE n.id =$nodeId DETACH DELETE n,child",
                Collections.singletonMap("nodeId", commitNode.getId()));
    }
}

