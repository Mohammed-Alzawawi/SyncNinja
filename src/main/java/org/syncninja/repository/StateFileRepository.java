package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.Neo4jSession;
import org.syncninja.model.StateFile;
import java.util.Optional;

public class StateFileRepository {
    public Optional<StateFile> findById(String path){
        Session session = Neo4jSession.getSession();
        StateFile stateFile = session.load(StateFile.class , path);
        return Optional.ofNullable(stateFile);
    }
    public void save(StateFile stateFile){
        Session session = Neo4jSession.getSession();
        session.save(stateFile);
    }
    public boolean existsById(String path) {
        return findById(path)!=null;
    }
}
