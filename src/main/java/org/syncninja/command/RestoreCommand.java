package org.syncninja.command;

import org.neo4j.ogm.session.Session;
import org.syncninja.service.StateTreeService;
import org.syncninja.util.Neo4jSession;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "restore")
public class RestoreCommand extends BaseCommand {
    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files to restore")
    private final List<String> restoreFiles = new ArrayList<>();

    private final StateTreeService stateTreeService;

    public RestoreCommand() {
        stateTreeService = new StateTreeService();
    }

    @Override
    public void run() {
        try {
            Session session = Neo4jSession.getSession();
            session.beginTransaction();

            String path = System.getProperty("user.dir");

            stateTreeService.restore(restoreFiles, path);

            session.getTransaction().commit();
            Neo4jSession.closeSession();

        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
