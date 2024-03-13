package org.syncninja.repository;

import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;

import java.util.Collection;
import java.util.Optional;

public class CommitNodeRepository {
    public void save(CommitNode commitNode) {
        Session session = Neo4jSession.getSession();
        session.save(commitNode);
    }

    public Optional<CommitNode> findByPath(String path) {
        Session session = Neo4jSession.getSession();
        Filter filter = new Filter("path", ComparisonOperator.EQUALS, path);
        Collection<CommitNode> commitNodes = session.loadAll(CommitNode.class, filter, -1);
        return Optional.ofNullable((commitNodes.isEmpty()) ? null : commitNodes.iterator().next());
    }

    public void deleteByPath(String path) throws Exception {
        CommitNode commitNode = findByPath(path).orElseThrow(()->{return new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.FILE_NOT_FOUND));});
        //CommitFile commitFile = findByPath(path).orElseThrow(()-> {return new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.FILE_NOT_FOUND));});
        Session session= Neo4jSession.getSession();
        session.delete(commitNode);
        //session.delete(commitFile);

    }
}

