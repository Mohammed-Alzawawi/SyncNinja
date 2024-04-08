package org.syncninja.command;

import org.neo4j.ogm.session.Session;
import org.syncninja.model.Branch;
import org.syncninja.model.Directory;
import org.syncninja.service.CommitService;
import org.syncninja.service.DirectoryService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

@CommandLine.Command(name = "init", description = "Initialize directory")
public class InitCommand extends BaseCommand {

    private final DirectoryService directoryService;
    private final StateTreeService stateTreeService;
    private final CommitService commitService;

    public InitCommand() {
        this.directoryService = new DirectoryService();
        this.stateTreeService = new StateTreeService();
        this.commitService = new CommitService();
    }

    @Override
    public void run() {
        try {
            Session session = Neo4jSession.getSession();
            session.beginTransaction();

            String path = System.getProperty("user.dir");
            Directory directory = directoryService.createDirectory(path);
            // creating main branch
            directoryService.createDirectoryMainBranch(directory, "main");
            Branch mainBranch = directory.getBranch();
            // creating staging area
            mainBranch.setNextCommit(commitService.createStagedCommit());
            // creating state tree
            stateTreeService.generateStateRootNode(path, mainBranch);

            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.DIRECTORY_INITIALIZED_SUCCESSFULLY, new Object[]{path}));
            session.getTransaction().commit();
            Neo4jSession.closeSession();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}