package SyncNinjaPackage.syncNinja.command;
import SyncNinjaPackage.syncNinja.service.CommitTreeService;
import SyncNinjaPackage.syncNinja.util.SpringAdapter;
import picocli.CommandLine;
import java.io.IOException;

@CommandLine.Command (name="add")
public class AddCommand implements Runnable{
    CommitTreeService commitTreeService = SpringAdapter.getBean(CommitTreeService.class);

    @Override
    public void run() {
        String path = System.getProperty("user.dir");
        try {
            commitTreeService.addFilesFromDirectoryToCommitTree(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}