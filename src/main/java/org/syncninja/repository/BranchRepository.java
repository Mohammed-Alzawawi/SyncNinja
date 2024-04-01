package org.syncninja.repository;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.syncninja.model.Branch;
import org.syncninja.model.Commit;
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
    public Optional<Result> getPathOfNinjaNodes(Commit currentCommit , Commit targetCommit){
        Session session = Neo4jSession.getSession();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("startId", currentCommit.getId());
        parameters.put("endId", targetCommit.getId());
        Result result = session.query("MATCH path = shortestPath((start:Commit {id: $startId})-[:nextCommit|ParentOf*]-(end:Commit {id: $endId})) RETURN nodes(path) AS nodes_on_path,  [rel in relationships(path) | {startNodeId: startNode(rel).id, endNodeId: endNode(rel).id}] AS relationships_on_path"
                , parameters);
        return Optional.ofNullable(result);
    }
}
