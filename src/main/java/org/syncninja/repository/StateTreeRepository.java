package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.Branch;
import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.statetree.StateDirectory;
import org.syncninja.model.statetree.StateNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.util.Fetcher;
import org.syncninja.util.Neo4jSession;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class StateTreeRepository {

    public Optional<StateNode> findById(String path) {
        Session session = Neo4jSession.getSession();
        StateNode stateNodeNode = session.load(StateNode.class, path, -1);
        return Optional.ofNullable(stateNodeNode);
    }

    public void save(StateNode stateNode) {
        Session session = Neo4jSession.getSession();
        session.save(stateNode);
    }

    public void updateStateRoot(StateRoot stateRoot, NinjaNode ninjaNode) {
        Session session = Neo4jSession.getSession();
        deleteSingleRelation(stateRoot, "CURRENT_BRANCH");
        deleteSingleRelation(stateRoot, "CURRENT_COMMIT");
        if (ninjaNode instanceof Commit) {
            stateRoot.setCurrentCommit((Commit) ninjaNode);
        } else {
            Branch branch = (Branch) ninjaNode;
            stateRoot.setCurrentBranch(branch);
            if (branch.hasLastCommit()) {
                stateRoot.setCurrentCommit(branch.getLastCommit());
            } else {
                stateRoot.setCurrentCommit(null);
            }
        }
        session.save(stateRoot);
    }

    public void delete(StateNode stateNode) {
        if (stateNode instanceof StateDirectory) {
            deleteDirectory(stateNode);
        } else {
            deleteFile(stateNode);
        }
    }

    public void deleteDirectory(StateNode stateNode) {
        Session session = Neo4jSession.getSession();
        session.query("MATCH (n:StateNode)-[*]->(child:StateNode) WHERE n.path =$path DETACH DELETE n,child",
                Collections.singletonMap("path", stateNode.getPath()));
    }

    public void deleteFile(StateNode stateNode) {
        Session session = Neo4jSession.getSession();
        session.query("MATCH (n:StateNode) WHERE n.path =$path DETACH DELETE n",
                Collections.singletonMap("path", stateNode.getPath()));
    }

    public void deleteNodeList(Set<StateNode> nodesToDelete) {
        Session session = Neo4jSession.getSession();
        if (nodesToDelete.isEmpty()) {
            return;
        }
        StringBuilder queryBuilder = new StringBuilder("MATCH (n:StateNode) WHERE n.path IN [");
        for (StateNode stateNode : nodesToDelete) {
            queryBuilder.append("'").append(Fetcher.getPathForQuery(stateNode.getPath())).append("',");
        }
        queryBuilder.deleteCharAt(queryBuilder.length() - 1); // Remove the trailing comma
        queryBuilder.append("] DETACH DELETE n");
        session.query(queryBuilder.toString(), Collections.emptyMap());
    }

    public void deleteSingleRelation(StateRoot stateRoot, String relationShip) {
        Session session = Neo4jSession.getSession();
        session.query(String.format("MATCH (n:StateRoot) -[r:%s]->(n1:NinjaNode) WHERE n.path = '%s' DELETE r",
                        relationShip, Fetcher.getPathForQuery(stateRoot.getPath())),
                Collections.emptyMap());
    }
}
