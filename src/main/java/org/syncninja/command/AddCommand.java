package org.syncninja.command;

import org.neo4j.ogm.session.Session;
import org.syncninja.service.CommitTreeService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.Neo4jSession;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "add", description = "add changes to staging area")
public class AddCommand extends BaseCommand {

    private final CommitTreeService commitTreeService;
    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files")
    private final List<String> filesToAdd = new ArrayList<>();

    public AddCommand() {
        commitTreeService = new CommitTreeService();
    }

    @Override
    public void run() {
        try {
            Session session = Neo4jSession.getSession();
            session.beginTransaction();
            String mainDirectoryPath = System.getProperty("user.dir");
            commitTreeService.addFileToCommitTree(mainDirectoryPath, filesToAdd);
            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_ADDED));
            session.getTransaction().commit();
            Neo4jSession.closeSession();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}