package org.syncninja.command;

import org.neo4j.ogm.session.Session;
import org.syncninja.service.CommitTreeService;
import org.syncninja.util.Neo4jSession;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "unstage", description = "unstage staged changes")
public class UnstageCommand extends BaseCommand {

    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files to unstage")
    private final List<String> unstageFiles = new ArrayList<>();

    private final CommitTreeService commitTreeService;

    public UnstageCommand() {
        this.commitTreeService = new CommitTreeService();
    }

    @Override
    public void run() {
        try {
            Session session = Neo4jSession.getSession();
            session.beginTransaction();

            String path = System.getProperty("user.dir");
            commitTreeService.unstage(path, unstageFiles);

            session.getTransaction().commit();
            Neo4jSession.closeSession();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}