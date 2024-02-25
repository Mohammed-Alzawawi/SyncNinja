package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import org.syncninja.model.Directory;
import org.syncninja.util.Neo4jSession;

import java.util.Optional;

public class DirectoryRepository {
    public Directory save(Directory directory){
        Session session = Neo4jSession.getSession();
        try(Transaction transaction = session.beginTransaction()){
            session.save(directory);
            transaction.commit();
        }
        return directory;
    }

    public Optional<Directory> findById(String path) {
        Session session = Neo4jSession.getSession();
        try (Transaction transaction = session.beginTransaction()) {
            Directory directory = session.load(Directory.class, path);
            transaction.commit();
            return Optional.ofNullable(directory);
        }
    }
}
