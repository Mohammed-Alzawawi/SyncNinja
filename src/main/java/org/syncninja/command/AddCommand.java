package org.syncninja.command;
import org.syncninja.service.CommitTreeService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command (name="add", description = "Add files to the commit tree")
public class AddCommand implements Runnable{

    private CommitTreeService commitTreeService;

    @CommandLine.Option(names = {"-a", "--all"}, paramLabel = "DIRECTORY", description = "Add a whole directory")
    private String directory;
    @CommandLine.Option(names = {"-f", "--file"}, description = "Add a specific file", arity = "1..*")
    private List<String> listOfFilesToAdd = new ArrayList<>();
=======
@CommandLine.Command (name="add", description = "Add files to the commit tree")
public class AddCommand implements Runnable{

private CommitTreeService commitTreeService;
    @CommandLine.Option(names = {"-a", "--all"}, paramLabel = "DIRECTORY", description = "Add a whole directory")
    private String directory;
    @CommandLine.Option(names = {"-f", "--file"}, description = "Add a specific file")
    private String filePath;
>>>>>>> fcc694a465f8362f5312a0334f4eca9ef014cdf9

    public AddCommand() {
        this.commitTreeService = new CommitTreeService();
    }

    @Override
    public void run() {
        try {
<<<<<<< HEAD
            String mainDirectoryPath = directory != null ? directory : System.getProperty("user.dir");
            commitTreeService.addFileToCommitTree(mainDirectoryPath, listOfFilesToAdd);
=======
            if (directory != null) {
                commitTreeService.addDirectoryToCommitTree(directory);
            } else if (filePath != null) {
                commitTreeService.addFileToCommitTree(filePath);
            } else {
                String path = System.getProperty("user.dir");
                commitTreeService.addFilesFromDirectoryToCommitTree(path);
            }
>>>>>>> fcc694a465f8362f5312a0334f4eca9ef014cdf9
            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_ADDED, new Object[]{}));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}