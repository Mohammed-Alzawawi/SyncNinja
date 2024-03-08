package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.Commit;
import org.syncninja.model.StateTree.StateRoot;
import org.syncninja.model.StateTree.StateTree;
import org.syncninja.util.Neo4jSession;

import java.util.Optional;

public class StateTreeRepository {

    public Optional<StateTree> findById(String path) throws Exception {
        Session session = Neo4jSession.getSession();
        StateTree stateTreeNode = session.load(StateTree.class, path, -1);
        return Optional.ofNullable(stateTreeNode);
    }

    public void save(StateTree stateTree) {
        Session session = Neo4jSession.getSession();
        session.save(stateTree);
    }

    public void updateStateRoot(StateRoot stateRoot, Commit commit){
        Session session = Neo4jSession.getSession();
        stateRoot.setCurrentCommit(commit);
        session.save(stateRoot);
    }
}