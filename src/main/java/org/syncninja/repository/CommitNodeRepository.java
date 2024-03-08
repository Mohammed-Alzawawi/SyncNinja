package org.syncninja.repository;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.syncninja.model.Directory;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.util.Neo4jSession;

import java.util.Collections;
import java.util.Optional;

public class CommitNodeRepository {
    public void save(CommitNode commitNode){
        Session session = Neo4jSession.getSession();
        session.save(commitNode);
    }
    public CommitNode getCommitNode(String path){
        Session session = Neo4jSession.getSession();
        CommitNode commitNode = session.load(CommitNode.class,path);
        return commitNode;
    }
    public Optional<Directory> findById(String id) {
        Session session = Neo4jSession.getSession();
        Directory directory = session.load(Directory.class, id);
        return Optional.ofNullable(directory);
    }


    public Optional<CommitNode> findByPath(String mainDirectoryPath) {
        Session session = Neo4jSession.getSession();
        Result result = session.query("MATCH(d:CommitNode) WHERE d.path = $path RETURN d", Collections.singletonMap("path", mainDirectoryPath));
        if (result.iterator().hasNext()) {
            return Optional.of((CommitNode) result.iterator().next().get("d"));
        } else {
            return Optional.empty();
        }
    }
}

