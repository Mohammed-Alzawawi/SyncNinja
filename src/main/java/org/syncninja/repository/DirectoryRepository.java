package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.Directory;
import org.syncninja.util.Neo4jSession;

import java.util.Collections;
import java.util.Optional;

public class DirectoryRepository {
    public Directory save(Directory directory){
        Session session = Neo4jSession.getSession();
        session.save(directory);
        return directory;
    }

    public Optional<Directory> findById(String id) {
        Session session = Neo4jSession.getSession();
        Directory directory = session.load(Directory.class, id);
        return Optional.ofNullable(directory);
    }

    public Optional<Directory> findByPath(String path) {
        Session session = Neo4jSession.getSession();
        Directory directory = session.queryForObject(Directory.class, "MATCH(d:Directory) WHERE d.path = $path RETURN d", Collections.singletonMap("path", path));
        return Optional.ofNullable(directory);
    }
}