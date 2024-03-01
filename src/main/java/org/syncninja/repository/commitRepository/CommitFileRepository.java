package org.syncninja.repository.commitRepository;
import org.syncninja.model.commitTree.CommitFile;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;

import static org.neo4j.driver.Values.parameters;

public class CommitFileRepository {

    private final Driver driver;

    public CommitFileRepository(Driver driver) {
        this.driver = driver;
    }

    public CommitFile findById(String id) {
        try (Session session = driver.session()) {
            Node node = session.readTransaction(tx -> tx.run("MATCH (n:CommitFile) WHERE id(n) = $id RETURN n", parameters("id", id)))
                    .single()
                    .get("n").asNode();
            return convertNodeToCommitFile(node);
        }
    }

    public void save(CommitFile commitFile) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
               // tx.run("CREATE (n:CommitFile {id: $id, property: $property})", parameters("id", commitFile.getId(), "property", commitFile.getProperty()));
                return null;
            });
        }
    }

    public void deleteById(String id) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n:CommitFile) WHERE id(n) = $id DELETE n", parameters("id", id));
                return null;
            });
        }
    }

    private CommitFile convertNodeToCommitFile(Node node) {
        //return new CommitFile(/*conversion?);
    }
}
