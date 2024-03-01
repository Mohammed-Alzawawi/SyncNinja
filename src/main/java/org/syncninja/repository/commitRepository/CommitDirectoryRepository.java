package SyncNinjaPackage.syncNinja.repository.commitRepository;
import org.syncninja.model.commitTree.CommitDirectory;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;

import static org.neo4j.driver.Values.parameters;

public class CommitDirectoryRepository {

    private final Driver driver;

    public CommitDirectoryRepository(Driver driver) {
        this.driver = driver;
    }

    public CommitDirectory findById(String id) {
        try (Session session = driver.session()) {
            Node node = session.readTransaction(tx -> tx.run("MATCH (n:CommitDirectory) WHERE id(n) = $id RETURN n", parameters("id", id)))
                    .single()
                    .get("n").asNode();
            return convertNodeToCommitDirectory(node);
        }
    }

    private CommitDirectory convertNodeToCommitDirectory(Node node) {
    }

    public void save(CommitDirectory commitDirectory) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                //tx.run("CREATE (n:CommitDirectory {id: $id, property: $property})", parameters("id", commitDirectory.getId(), "property", commitDirectory.getProperty()));
                return null;
            });
        }
    }

    public void deleteById(String id) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n:CommitDirectory) WHERE id(n) = $id DELETE n", parameters("id", id));
                return null;
            }
            );
        }
    }

   
}
