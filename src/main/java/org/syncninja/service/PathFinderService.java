package org.syncninja.service;

import org.neo4j.ogm.model.Result;
import org.syncninja.dto.FileStatusEnum;
import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.committree.CommitDirectory;
import org.syncninja.model.committree.CommitFile;
import org.syncninja.model.committree.CommitNode;
import org.syncninja.model.statetree.StateDirectory;
import org.syncninja.model.statetree.StateFile;
import org.syncninja.model.statetree.StateNode;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.util.ResourceBundleEnum;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PathFinderService {

    private final StateTreeRepository stateTreeRepository;

    public PathFinderService() {
        this.stateTreeRepository = new StateTreeRepository();
    }

    protected NinjaNode getAncestorNode(Map<String, String>[] relationships, ArrayList<NinjaNode> ninjaNodes) {
        Map<String, Integer> occurrenceOfEachNode = new HashMap<>();
        String ancestorId = null;
        for (Map<String, String> map : relationships) {
            String startNodeId = map.get("startNodeId");
            occurrenceOfEachNode.put(startNodeId, occurrenceOfEachNode.getOrDefault(startNodeId, 0) + 1);
            if (occurrenceOfEachNode.getOrDefault(startNodeId, 0) == 2) {
                ancestorId = startNodeId;
            }
        }
        for (NinjaNode ninjaNode : ninjaNodes) {
            if (ninjaNode.getId().equals(ancestorId)) {
                return ninjaNode;
            }
        }
        return null;
    }

    protected Map<String, String>[] getRelationshipsInPath(Result result) {
        @SuppressWarnings("unchecked") Map<String, String>[] relationships = (Map<String, String>[]) result.queryResults().iterator().next().get("relationships_on_path");
        return relationships;
    }

    protected ArrayList<NinjaNode> getNinjaNodesInPath(Result result) {
        @SuppressWarnings("unchecked") ArrayList<NinjaNode> ninjaNodes = (ArrayList<NinjaNode>) result.queryResults().iterator().next().get("nodes_on_path");
        return ninjaNodes;
    }

    protected void findOutOfAncestor(ArrayList<NinjaNode> listOfCommitsToMerge, NinjaNode ancestorNode, ArrayList<NinjaNode> nodesInPath, Map<String, String>[] relationships) {
        if (ancestorNode != null) {
            int indexOfAncestor = nodesInPath.indexOf(ancestorNode);
            for (int i = indexOfAncestor + 1; i < nodesInPath.size(); i++) {
                NinjaNode node = nodesInPath.get(i);
                if (node instanceof Commit) {
                    listOfCommitsToMerge.add(node);
                }
            }
        } else {
            if (relationships[0].get("startNodeId").equals(nodesInPath.get(0).getId())) {
                for (int i = 1; i < nodesInPath.size(); i++) {
                    NinjaNode node = nodesInPath.get(i);
                    if (node instanceof Commit) {
                        listOfCommitsToMerge.add(node);
                    }
                }
            }
        }
    }

    protected void findGoingToAncestor(ArrayList<NinjaNode> listOfCommitsToIgnore, NinjaNode ancestorNode, ArrayList<NinjaNode> nodesInPath, Map<String, String>[] relationships) {
        if (ancestorNode != null) {
            int indexOfAncestor = nodesInPath.indexOf(ancestorNode);
            for (int i = 0; i < indexOfAncestor; i++) {
                NinjaNode node = nodesInPath.get(i);
                if (node instanceof Commit) {
                    listOfCommitsToIgnore.add(node);
                }
            }
        } else {
            //target branch is the parent of the current branch that means all the commits in the path should be removed
            if (relationships[0].get("endNodeId").equals(nodesInPath.get(0).getId())) {
                for (int i = 0; i < nodesInPath.size() - 1; i++) {
                    NinjaNode node = nodesInPath.get(i);
                    if (node instanceof Commit) {
                        listOfCommitsToIgnore.add(node);
                    }
                }
            }
        }
    }
}
