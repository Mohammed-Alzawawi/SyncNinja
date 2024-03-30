package org.client.command;

import org.codehaus.jettison.json.JSONObject;
import picocli.CommandLine;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "restore")
public class RestoreCommand extends BaseCommand {

    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files to restore")
    private final List<String> restoreFiles = new ArrayList<>();

    @Override
    public void run() {
        try{
            HttpURLConnection connection = serverConnection("restore");

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("path", System.getProperty("user.dir"));
            jsonRequest.put("files", restoreFiles);
            sendToServer(jsonRequest, connection);

            getResponse(connection);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}