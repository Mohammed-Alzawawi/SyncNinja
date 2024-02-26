package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.Neo4jSession;
import org.syncninja.model.StateDirectory;
import org.syncninja.model.StateFile;

import java.util.Optional;

public class StateDirectoryRepository {
    public Optional<StateDirectory> findById(String path){
        Session session = Neo4jSession.getSession();
        StateDirectory stateDirectory = session.load(StateDirectory.class , path);
        return Optional.ofNullable(stateDirectory);
    }
    public void save(StateDirectory stateDirectory){
        Session session = Neo4jSession.getSession();
        session.save(stateDirectory);
    }

    public boolean existsById(String path) {
        return findById(path)!=null;
    }
}
