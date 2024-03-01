package org.syncninja.repository;

import org.neo4j.ogm.session.Session;

import org.syncninja.model.StateTree.StateDirectory;
import org.syncninja.model.StateTree.StateTree;
import org.syncninja.util.Neo4jSession;

import java.util.*;

public class StateTreeRepository {

    public Optional<StateTree> findById(String path) throws Exception {
        Session session = Neo4jSession.getSession();
        StateTree stateTreeNode = session.load(StateTree.class, path);
        return Optional.ofNullable(stateTreeNode);

    }

    public void save(StateTree stateTree) {
        Session session = Neo4jSession.getSession();
        session.save(stateTree);
    }
}
