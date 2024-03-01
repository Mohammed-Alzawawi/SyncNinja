package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.StateTree;
import org.syncninja.model.StateDirectory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StateDirectoryRepository {
    ResourceMessagingService resourceMessagingService = new ResourceMessagingService();
    public Optional<StateDirectory> findById(String path) throws Exception {
        Session session = Neo4jSession.getSession();
        StateDirectory stateDirectory = session.load(StateDirectory.class, path);
        return Optional.ofNullable(stateDirectory);
    }
    public void save(StateDirectory stateDirectory){
        Session session = Neo4jSession.getSession();
        session.save(stateDirectory);
    }


//    public StateTree loadByDepth(String mainPath , String wantedNode) throws Exception {
//        Session session = Neo4jSession.getSession();
//        StateDirectory stateTree = session.load(StateDirectory.class , mainPath , 5);
//        List<StateTree> stateDirectoryList = new ArrayList<>();
//        if(stateTree==null){
//            return null;        }
//        if(stateTree.getPath().equals(wantedNode)){
//            return stateTree;
//        }
//        for(StateTree node : stateTree.getInternalNodes()){
//
//            if(node.getPath().equals(wantedNode)){
//                return node;
//            }
//            if(node.isDirectory()){
//                stateDirectoryList.add(node);
//            }
//        }
//        for(StateTree node: stateDirectoryList){
//            StateTree stateTree1 = loadByDepth(node.getPath() , wantedNode);
//            if(stateTree1!=null){
//                return stateTree1;
//            }
//        }
//        return null;
//
//
//    }
}
