package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.Branch;
import org.syncninja.util.Neo4jSession;

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
}
