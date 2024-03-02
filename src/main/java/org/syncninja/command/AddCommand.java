package org.syncninja.command;

import org.syncninja.model.StateTree.StateRoot;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.DirectoryRepository;
import org.syncninja.repository.StateTreeRepository;
import org.syncninja.service.CommitTreeService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

@CommandLine.Command (name="add")
public class AddCommand implements Runnable{
private CommitTreeService commitTreeService;
private StateTreeRepository stateTreeRepository;

    public AddCommand() {
        this.commitTreeService = new CommitTreeService();
        this.stateTreeRepository = new StateTreeRepository();
    }

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        try {
            CommitNode commitNode = commitTreeService.addFilesFromDirectoryToCommitTree(path);
            StateRoot stateRoot = (StateRoot) stateTreeRepository.findById(path).orElse(null);
            stateRoot.setCurrentCommit(commitNode);
            stateTreeRepository.save(stateRoot);
            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_ADDED, new Object[]{}));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}