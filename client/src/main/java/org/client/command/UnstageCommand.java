package org.client.command;

import org.codehaus.jettison.json.JSONObject;
import picocli.CommandLine;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "unstage", description = "unstage staged changes")
public class UnstageCommand extends BaseCommand {

    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files to unstage")
    private final List<String> unstageFiles = new ArrayList<>();

    @Override
    public void run() {
        try{
            HttpURLConnection connection = serverConnection("unstage");

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("path", System.getProperty("user.dir"));
            jsonRequest.put("files", unstageFiles);
            sendToServer(jsonRequest, connection);

            getResponse(connection);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}