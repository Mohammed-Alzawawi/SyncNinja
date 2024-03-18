package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.Commit;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.model.statetree.StateNode;
import org.syncninja.util.Neo4jSession;

import java.util.Optional;

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

    public void updateStateRoot(StateRoot stateRoot, Commit commit){
        Session session = Neo4jSession.getSession();
        stateRoot.setCurrentCommit(commit);
        session.save(stateRoot);
    }
}