package org.syncninja.repository;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.syncninja.model.Branch;
import org.syncninja.model.NinjaNode;
import org.syncninja.util.Neo4jSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BranchRepository {

    public void save(Branch branch) {
        Session session = Neo4jSession.getSession();
        session.save(branch);
    }

    public Optional<Branch> findByName(String branchName,String path){
        Session session = Neo4jSession.getSession();
        String cypher = "match(n:Directory)-[*]->(b:Branch)where n.path=$path and b.name=$branchName return b";
        Branch branch = session.queryForObject(Branch.class, cypher, Map.of("path", path, "branchName", branchName));
        return Optional.ofNullable(branch);
    }

    public Optional<Result> getPathOfNinjaNodes(NinjaNode currentNode , NinjaNode targetNode){
        Session session = Neo4jSession.getSession();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("startId", currentNode.getId());
        parameters.put("endId", targetNode.getId());
        Result result = session.query("MATCH path = shortestPath((start:NinjaNode {id: $startId})-[:nextCommit|ParentOf*]-(end:NinjaNode {id: $endId})) RETURN nodes(path) AS nodes_on_path,  [rel in relationships(path) | {startNodeId: startNode(rel).id, endNodeId: endNode(rel).id}] AS relationships_on_path"
                , parameters);
        return Optional.ofNullable(result);
    }
}
