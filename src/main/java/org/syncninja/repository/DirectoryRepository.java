package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.Directory;
import org.syncninja.util.Neo4jSession;

import java.util.Optional;

public class DirectoryRepository {
    public Directory save(Directory directory){
        Session session = Neo4jSession.getSession();
        session.save(directory);
        return directory;
    }

    public Optional<Directory> findById(String path) {
        Session session = Neo4jSession.getSession();
        Directory directory = session.load(Directory.class, path);
        return Optional.ofNullable(directory);
    }
}
