package org.syncninja.repository;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.StateDirectory;
import org.syncninja.model.StateTree;

import java.util.*;

public class StateTreeRepository {
    ResourceMessagingService resourceMessagingService = new ResourceMessagingService();
    public StateTree findById(String path) throws Exception {
        Session session = Neo4jSession.getSession();
        StateTree stateTreeNode = session.load(StateTree.class, path);
        return stateTreeNode;

    }
//    public StateTree loadByDepth(String mainPath , String wantedNode) throws Exception {
//        Session session = Neo4jSession.getSession();
//        StateDirectory stateTree = session.load(StateDirectory.class , mainPath , 5);
//        List<StateTree> stateDirectoryList = new ArrayList<>();
//        if(stateTree==null){
//            return null;
//        }
//        if(stateTree.getPath().equals(wantedNode)){
//            return stateTree;
//        }
//        for(StateTree node : stateTree.getInternalNodes()){
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


    public void save(StateTree stateTree) {
        Session session = Neo4jSession.getSession();
        session.save(stateTree);
    }
}
