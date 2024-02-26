package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import org.syncninja.Neo4jSession;
import org.syncninja.model.StateDirectory;
import org.syncninja.model.StateTree;

import java.util.*;

public class StateTreeRepository {

    public Optional<StateTree> findById(String path){
        Session session = Neo4jSession.getSession();
        StateTree stateTreeNode;
        try(Transaction transaction = session.beginTransaction()){
            stateTreeNode = loadByDepth(path);
            transaction.commit();
        }

        return Optional.ofNullable(stateTreeNode);

    }
    public StateTree loadByDepth(String path){
        Session session = Neo4jSession.getSession();
        StateTree stateTree = session.load(StateTree.class , path , 3);
        if(stateTree!=null){
            return stateTree;
        }
        else{
            Iterable<StateDirectory> stateTrees = session.query(StateDirectory.class ,
                    "MATCH (startNode:StateDirectory)-[:HAS*0..3]-(attachedNodes:StateDirectory) RETURN attachedNodes;" ,
                    Collections.singletonMap("path" , null));

            List<StateDirectory> nodes = new ArrayList<>();
            stateTrees.forEach(nodes::add);
            ListIterator<StateDirectory> listIterator = nodes.listIterator();

            while (listIterator.hasNext()){
                if(loadByDepth(path , listIterator.next().getPath())!=null){
                    stateTree = loadByDepth(path , listIterator.next().getPath());
                    break;
                }
                else{
                    session.query(StateDirectory.class ,
                            "MATCH (startNode:StateDirectory)-[:HAS*0..3]-(attachedNodes:StateDirectory) WHERE startNode.path = $path RETURN attachedNodes;" ,
                            Collections.singletonMap("path", listIterator.next().getPath())).forEach(listIterator::add);
                }
            }
        }
        return stateTree;

    }
    public StateTree loadByDepth(String path , String startingNode){
        Session session = Neo4jSession.getSession();
        HashMap<String , String> hashMap = new HashMap<>();
        hashMap.put("startingNode" , startingNode);
        hashMap.put("path" , path);
         return session.queryForObject(StateTree.class ,
                "MATCH (startingN {path : $startingNode})-[:HAS*0..3]-(wantedNode {path : $path}) RETURN wantedNode;" ,
                hashMap) ;


    }


}
