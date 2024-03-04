package org.syncninja.command;
import org.syncninja.service.CommitTreeService;
import org.syncninja.service.ResourceMessagingService;
import org.syncninja.util.ResourceBundleEnum;
import picocli.CommandLine;

@CommandLine.Command (name="add", description = "Add files to the commit tree")
public class AddCommand implements Runnable{

private CommitTreeService commitTreeService;
    @CommandLine.Option(names = {"-a", "--all"}, paramLabel = "DIRECTORY", description = "Add a whole directory")
    private String directory;
    @CommandLine.Option(names = {"-f", "--file"}, description = "Add a specific file")
    private String filePath;

    public AddCommand() {
        this.commitTreeService = new CommitTreeService();
    }

    @Override
    public void run() {
        try {
            if (directory != null) {

                commitTreeService.addFilesFromDirectoryToCommitTree(directory);
            } else if (filePath != null) {
                String mainDirectoryPath = System.getProperty("user.dir");
                commitTreeService.addFileToCommitTree(mainDirectoryPath,filePath);
            } else {
                String path = System.getProperty("user.dir");
                commitTreeService.addFilesFromDirectoryToCommitTree(path);
            }
            System.out.println(ResourceMessagingService.getMessage(ResourceBundleEnum.SUCCESSFULLY_ADDED, new Object[]{}));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}