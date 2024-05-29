package org.syncninja.command;

import org.neo4j.ogm.session.Session;
import org.syncninja.service.MergeService;
import org.syncninja.util.Neo4jSession;
import picocli.CommandLine;

@CommandLine.Command(name = "merge", description = "merge two branches")
public class MergeCommand extends BaseCommand {

    @CommandLine.Parameters(paramLabel = "branch", description = "Specify a branch to merge")
    private String branchName;

    private final MergeService mergeService;

    public MergeCommand() {
        this.mergeService = new MergeService();
    }

    @Override
    public void run() {
        try {
            Session session = Neo4jSession.getSession();
            session.beginTransaction();

            String path = System.getProperty("user.dir");
            mergeService.merge(path, branchName);

            session.getTransaction().commit();
            Neo4jSession.closeSession();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
