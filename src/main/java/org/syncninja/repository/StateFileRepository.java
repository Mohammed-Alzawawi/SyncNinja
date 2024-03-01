package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.StateFile;
import org.syncninja.util.Neo4jSession;

import java.util.Optional;

public class StateFileRepository {
    public Optional<StateFile> findById(String path) throws Exception {
        Session session = Neo4jSession.getSession();
        StateFile stateFile = session.load(StateFile.class, path);
        return Optional.ofNullable(stateFile);
    }
}
