package org.syncninja.util;

import org.syncninja.model.Commit;
import org.syncninja.model.committree.CommitDirectory;
import org.syncninja.model.committree.CommitFile;
import org.syncninja.model.committree.CommitNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombineCommitsUtil {
    public static Map<String, CommitNode> combineCommits(List<Commit> commitList) {
        Map<String, CommitNode> combinedCommitNodeMap = new HashMap<>();
        for (Commit commit : commitList) {
            CommitDirectory commitTreeRoot = commit.getCommitTreeRoot();
            addCommitTreeToCombinedCommitList(combinedCommitNodeMap, commitTreeRoot);
        }
        return combinedCommitNodeMap;
    }

    private static void addCommitTreeToCombinedCommitList(Map<String, CommitNode> combinedCommitNodeMap, CommitDirectory commitDirectory) {
        for(CommitNode commitNode: commitDirectory.getCommitNodeList()) {
            if(commitNode instanceof CommitDirectory) {
                combinedCommitNodeMap.put(commitNode.getFullPath(), commitNode);
                addCommitTreeToCombinedCommitList(combinedCommitNodeMap, (CommitDirectory) commitNode);
            } else {
                // first time?
                if(!combinedCommitNodeMap.containsKey(commitNode.getFullPath())) {
                    combinedCommitNodeMap.put(commitNode.getFullPath(), commitNode);
                } else {
                    CommitFile oldCommitFile = (CommitFile) combinedCommitNodeMap.get(commitNode.getFullPath());
                    CommitFile newCommitFile = (CommitFile) commitNode;
                    CommitFile commitFile = combineCommitFiles(oldCommitFile, newCommitFile);
                    combinedCommitNodeMap.put(commitNode.getFullPath(), commitFile);
                }
            }
        }
    }
    private static CommitFile combineCommitFiles(CommitFile oldCommitFile, CommitFile newCommitFile) {
        List<Integer> commitFileLineNumberList = new ArrayList<>();
        List<String> commitFileNewValueList = new ArrayList<>();
        List<String> commitFileOldValueList = new ArrayList<>();

        List<Integer> newLineNumberList = newCommitFile.getLineNumberList();
        List<Integer> oldLineNumberList = oldCommitFile.getLineNumberList();

        int newIndex = 0, oldIndex = 0;
        while(newIndex < newLineNumberList.size() && oldIndex < oldLineNumberList.size()) {
            // if equal move them both but update the value to be from the new commit
            if ((int) newLineNumberList.get(newIndex) == oldLineNumberList.get(oldIndex)) {
                commitFileLineNumberList.add(newLineNumberList.get(newIndex));
                commitFileNewValueList.add(newCommitFile.getNewValuesList().get(newIndex));
                commitFileOldValueList.add(newCommitFile.getOldValuesList().get(newIndex));
                newIndex++;
                oldIndex++;
            } else if ((int) newLineNumberList.get(newIndex) < oldLineNumberList.get(oldIndex)) {
                commitFileLineNumberList.add(newLineNumberList.get(newIndex));
                commitFileNewValueList.add(newCommitFile.getNewValuesList().get(newIndex));
                commitFileOldValueList.add(newCommitFile.getOldValuesList().get(newIndex));
                newIndex++;
            } else {
                commitFileLineNumberList.add(oldLineNumberList.get(oldIndex));
                commitFileNewValueList.add(oldCommitFile.getNewValuesList().get(oldIndex));
                commitFileOldValueList.add(oldCommitFile.getOldValuesList().get(oldIndex));
                oldIndex++;
            }
        }
        return new CommitFile(newCommitFile.getPath(), newCommitFile.getStatusEnum(), commitFileLineNumberList, commitFileNewValueList, commitFileOldValueList);
    }
}
