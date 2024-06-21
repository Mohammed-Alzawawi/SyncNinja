package org.syncninja.service;

import org.neo4j.ogm.model.Result;
import org.syncninja.dto.CommitFileDTO;
import org.syncninja.dto.ConflictBundle;
import org.syncninja.dto.FileStatusEnum;
import org.syncninja.dto.StatusFileDTO;
import org.syncninja.model.Branch;
import org.syncninja.model.Commit;
import org.syncninja.model.NinjaNode;
import org.syncninja.model.SyncNode;
import org.syncninja.model.committree.CommitDirectory;
import org.syncninja.model.committree.CommitFile;
import org.syncninja.model.committree.CommitNode;
import org.syncninja.model.committree.mergetree.MergeCommit;
import org.syncninja.model.committree.mergetree.MergeDirectory;
import org.syncninja.model.committree.mergetree.MergeFile;
import org.syncninja.model.statetree.StateFile;
import org.syncninja.model.statetree.StateNode;
import org.syncninja.model.statetree.StateRoot;
import org.syncninja.repository.BranchRepository;
import org.syncninja.repository.CommitNodeRepository;
import org.syncninja.repository.CommitRepository;
import org.syncninja.util.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MergeService {
    private final BranchRepository branchRepository;
    private final StateTreeService stateTreeService;
    private final CommitNodeRepository commitNodeRepository;
    private final PathFinderService pathFinderService;
    private final CommitRepository commitRepository;
    private final StateTreeUpdate stateTreeUpdate;
    private final StatusService statusService;

    public MergeService() {
        this.branchRepository = new BranchRepository();
        this.stateTreeService = new StateTreeService();
        this.commitNodeRepository = new CommitNodeRepository();
        this.pathFinderService = new PathFinderService();
        this.commitRepository = new CommitRepository();
        this.stateTreeUpdate = new StateTreeUpdate();
        this.statusService = new StatusService();
    }

    public boolean merge(String path, String branchName) throws Exception {
        StateRoot stateRoot = stateTreeService.getStateRoot(path);
        Optional<Branch> branchOptional = branchRepository.findByName(branchName, path);

        if (branchOptional.isPresent()) {
            Branch branch = branchOptional.get();
            FileTrackingState state = statusService.getState(path);
            List<CommitFileDTO> tracked = state.getTracked();
            List<StatusFileDTO> untracked = state.getUntracked();

            if(tracked.isEmpty() && untracked.isEmpty()){
                if (!branch.getName().equals(stateRoot.getCurrentBranch().getName())) {
                    // get both sides of the path
                    NinjaNode currentNode = stateRoot.getCurrentNinjaNode();
                    NinjaNode targetNode = branch.getLastNinjaNode();
                    Result result = branchRepository.getPathOfNinjaNodes(currentNode, targetNode).get();

                    // get the relationships and nodes in the path
                    Map<String, String>[] relationships = pathFinderService.getRelationshipsInPath(result);
                    ArrayList<NinjaNode> ninjaNodesInPath = pathFinderService.getNinjaNodesInPath(result);

                    // use these arrays to update the file system and the state tree and they are sorted
                    CommitContainer commitContainer = new CommitContainer();
                    ArrayList<NinjaNode> mergedNinjaNodes = commitContainer.getCommitsToAdd();
                    ArrayList<NinjaNode> ignoredNinjaNodes = commitContainer.getCommitsToRemove();

                    // get the nodes to merge and the ones to ignore
                    NinjaNode ancestorNode = pathFinderService.getAncestorNode(relationships, ninjaNodesInPath);
                    pathFinderService.findOutOfAncestor(mergedNinjaNodes, ancestorNode, ninjaNodesInPath, relationships);
                    pathFinderService.findGoingToAncestor(ignoredNinjaNodes, ancestorNode, ninjaNodesInPath, relationships);

                    Map<String, NinjaNode> ignoredNinjaNodeMap = ignoredNinjaNodes.stream().collect(Collectors.toMap(NinjaNode::getId, (ninjaNode -> ninjaNode)));

                    // build a map which combines all ignored commit into one commit
                    List<Commit> ignoredCommitList = ignoredNinjaNodes.stream().filter(ninjaNode -> ninjaNode instanceof Commit)
                            .sorted(Comparator.comparing(SyncNode::getCreationTime))
                            .map(ninjaNode -> (Commit) ninjaNode)
                            .collect(Collectors.toList());
                    Map<String, CommitNode> combinedCommitsMap = CombineCommitsUtil.combineCommits(ignoredCommitList);

                    // StateRoot
                    Map<StateNode, FileStatusEnum> fileStateMap = new HashMap<>();
                    Map<String, StateNode> stateTree = stateTreeService.getStateTree(stateRoot);

                    if (mergeLogic(mergedNinjaNodes, ignoredNinjaNodeMap, currentNode, combinedCommitsMap, stateTree, fileStateMap, stateRoot)) {
                        return true;
                    }
                } else {
                    throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.ALREADY_IN_BRANCH, new Object[]{branchName}));
                }
            } else {
                throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.MERGE_FAILED_UNCOMMITTED_CHANGES));
            }
        } else {
            throw new Exception(ResourceMessagingService.getMessage(ResourceBundleEnum.BRANCH_NOT_FOUND, new Object[]{branchName}));
        }
        return false;
    }

    private boolean mergeLogic(ArrayList<NinjaNode> mergedNinjaNodes, Map<String, NinjaNode> ignoredNinjaNodeMap, NinjaNode currentNode, Map<String, CommitNode> combinedCommitsMap, Map<String, StateNode> stateTree, Map<StateNode, FileStatusEnum> fileStateMap, StateRoot stateRoot) throws Exception {
        // merge logic
        MergeCommit mergeCommit = null;
        for (NinjaNode ninjaNodeToMerge: mergedNinjaNodes) {
            if (!ignoredNinjaNodeMap.containsKey(ninjaNodeToMerge.getId()) && ninjaNodeToMerge instanceof Commit) {
                mergeCommit = new MergeCommit((Commit) ninjaNodeToMerge);
                MergeDirectory mergeTreeRoot = new MergeDirectory(((Commit) ninjaNodeToMerge).getCommitTreeRoot());
                mergeCommit.setCommitTree(mergeTreeRoot);

                // delete staging area if exist
                if(currentNode.getNextCommit() != null){
                    commitRepository.delete(currentNode.getNextCommit());
                }

                // merge commit logic
                ConflictBundle conflictBundle = mergeCommitTree(((Commit) ninjaNodeToMerge).getCommitTreeRoot(), mergeTreeRoot, combinedCommitsMap);

                stateTreeUpdate.updateStateTreeByAddingCommits(stateTree, List.of(mergeCommit), fileStateMap);

                if(conflictBundle.containsConflict()) {
                    // update file system with state tree
                    stateTreeUpdate.reflectStateTreeOnFileSystem(fileStateMap);
                    // create the conflict files
                    createConflictFiles(conflictBundle);

                    mergeCommit.setCommitted(false);
                    currentNode.setNextCommit(mergeCommit);
                    commitRepository.save(mergeCommit);
                    commitRepository.save((Commit) currentNode);

                    return true;
                } else {
                    Branch currentBranch = stateRoot.getCurrentBranch();
                    currentBranch.setLastCommit(mergeCommit);
                    new BranchRepository().save(currentBranch);
                    stateTreeService.updateStateRoot(stateRoot, mergeCommit);
                }
                commitRepository.save((Commit) ninjaNodeToMerge);
                currentNode = currentNode.getNextCommit();
            }
        }

        if (mergeCommit != null) {
            // adding staging area
            mergeCommit.setNextCommit(new Commit(false));
            commitRepository.save(mergeCommit);
            // update file system with state tree
            stateTreeUpdate.reflectStateTreeOnFileSystem(fileStateMap);
        }
        return false;
    }

    private ConflictBundle mergeCommitTree(CommitDirectory commitDirectory, MergeDirectory mergeDirectory, Map<String, CommitNode> combinedCommitsMap) {
        ConflictBundle conflictBundle = new ConflictBundle();
        mergeCommitTreeHelper(commitDirectory, mergeDirectory, combinedCommitsMap, conflictBundle);
        commitNodeRepository.save(mergeDirectory);
        return conflictBundle;
    }
    private void mergeCommitTreeHelper(CommitDirectory commitDirectory, MergeDirectory mergeDirectory, Map<String, CommitNode> combinedCommitsMap, ConflictBundle conflictBundle) {
        List<CommitNode> commitNodeList = commitDirectory.getCommitNodeList();

        for (CommitNode commitNode : commitNodeList) {
            CommitNode currentMergeNode = null;
            if (commitNode instanceof CommitFile) {
                if(combinedCommitsMap.containsKey(commitNode.getFullPath())) {
                    currentMergeNode = handleConflict(combinedCommitsMap, conflictBundle, commitNode, currentMergeNode);
                } else {
                    currentMergeNode = new MergeFile((CommitFile) commitNode);
                }
            } else {
                currentMergeNode = new MergeDirectory((CommitDirectory) commitNode);
                mergeCommitTreeHelper((CommitDirectory) commitNode, (MergeDirectory) currentMergeNode, combinedCommitsMap, conflictBundle);
            }

            if(currentMergeNode != null)
                mergeDirectory.getCommitNodeList().add(currentMergeNode);
        }
    }

    private CommitNode handleConflict(Map<String, CommitNode> combinedCommitsMap, ConflictBundle conflictBundle, CommitNode commitNode, CommitNode currentMergeNode) {
        StateNode stateNode = stateTreeService.findStateNodeByPath(commitNode.getFullPath());
        List<String> conflictFileLines = new ArrayList<>();

        CommitFile combinedCommitFile = (CommitFile) combinedCommitsMap.get(commitNode.getFullPath());
        CommitFile commitFile = (CommitFile) commitNode;
        
        if(stateNode == null) {
            // new files case
            conflictFileLines.add("<--- Current Commit changes --->");
            conflictFileLines.addAll(combinedCommitFile.getNewValuesList());
            conflictFileLines.add("<--- new Commit changes --->");
            conflictFileLines.addAll(commitFile.getNewValuesList());
            conflictFileLines.add("<--- end of changes --->");
            conflictBundle.getConflictFileMap().put(commitNode.getFullPath(), conflictFileLines);
        }
        else {
            StateFile stateFile = (StateFile) stateNode;
            List<String> lines = stateFile.getLines();
            boolean conflictDetected = false;
            conflictDetected = isConflictDetected(conflictFileLines, combinedCommitFile, commitFile, lines, conflictDetected);
            if(conflictDetected) {
                conflictBundle.getConflictFileMap().put(commitNode.getFullPath(), conflictFileLines);
            } else {
                // fixed without conflict
                MergeFile mergeFile = new MergeFile((CommitFile) commitNode);
                LinesContainer linesContainer = CompareFileUtil.compareNewAndOldLists(conflictFileLines, lines);
                mergeFile.setLineNumberList(linesContainer.getLineNumbers());
                mergeFile.setNewValuesList(linesContainer.getNewLines());
                mergeFile.setOldValuesList(linesContainer.getOldLines());
                currentMergeNode = mergeFile;
            }
        }
        return currentMergeNode;
    }

    private static boolean isConflictDetected(List<String> conflictFileLines, CommitFile combinedCommitFile, CommitFile commitFile, List<String> lines, boolean conflictDetected) {
        boolean reachedEnd = false;
        for(int index = 0; index < lines.size();) {
            boolean isLineInCombinedFile = combinedCommitFile.getLineNumberList().contains(index + 1);
            boolean isLineInCommitFile = commitFile.getLineNumberList().contains(index + 1);
            if(isLineInCombinedFile && isLineInCommitFile) {
                // Conflict
                conflictDetected = true;
                conflictFileLines.add("<--- Current Commit changes --->");
                int combinedCommitFileIndex = combinedCommitFile.getLineNumberList().indexOf(index + 1);
                int count = 1;

                while(combinedCommitFileIndex < combinedCommitFile.getLineNumberList().size()) {
                    if(index + count != combinedCommitFile.getLineNumberList().get(combinedCommitFileIndex)) {
                        break;
                    }
                    conflictFileLines.add(combinedCommitFile.getNewValuesList().get(combinedCommitFileIndex));
                    combinedCommitFileIndex++;
                    count++;
                }

                conflictFileLines.add("<--- New Commit changes --->");

                int commitFileIndex = commitFile.getLineNumberList().indexOf(index + 1);
                int count2 = 1;
                while(commitFileIndex < commitFile.getLineNumberList().size()) {
                    if(index + count2 != commitFile.getLineNumberList().get(commitFileIndex)) {
                        break;
                    }
                    conflictFileLines.add(commitFile.getNewValuesList().get(commitFileIndex));
                    commitFileIndex++;
                    count2++;
                }
                if(commitFileIndex == commitFile.getLineNumberList().size()) {
                    reachedEnd = true;
                }

                conflictFileLines.add("<--- End changes --->");
                index += count - 1;

            } else if(isLineInCommitFile) {
                int valueIndex = commitFile.getLineNumberList().indexOf(index + 1);
                conflictFileLines.add(commitFile.getNewValuesList().get(valueIndex));
                index++;
            } else {
                conflictFileLines.add(lines.get(index));
                index++;
            }
        }

        // added lines at the end of the commit
        if(!reachedEnd) {
            int valueIndex = commitFile.getLineNumberList().indexOf(lines.size() + 1);
            for(int index = valueIndex; index < commitFile.getLineNumberList().size() && valueIndex >= 0; index++){
                conflictFileLines.add(lines.get(index));
                index++;
            }
        }
        return conflictDetected;
    }

    private void createConflictFiles(ConflictBundle conflictBundle) throws IOException {
        for(String path: conflictBundle.getConflictFileMap().keySet()) {
            List<String> lines = conflictBundle.getConflictFileMap().get(path);
            Files.write(Path.of(path), lines);
        }
    }
}
