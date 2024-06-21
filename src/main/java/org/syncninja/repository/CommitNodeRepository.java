package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.committree.CommitNode;
import org.syncninja.util.Neo4jSession;

import java.util.Collections;
import java.util.Set;

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

    public void deleteNodeList(Set<CommitNode> nodesToDelete) {
        Session session = Neo4jSession.getSession();
        if (nodesToDelete.isEmpty()) {
            return;
        }
        StringBuilder queryBuilder = new StringBuilder("MATCH (n:CommitNode) WHERE n.id IN [");
        for (CommitNode commitNode : nodesToDelete) {
            queryBuilder.append("'").append(commitNode.getId()).append("',");
        }
        queryBuilder.deleteCharAt(queryBuilder.length() - 1); // Remove the trailing comma
        queryBuilder.append("] DETACH DELETE n");
        session.query(queryBuilder.toString(), Collections.emptyMap());
    }
}
