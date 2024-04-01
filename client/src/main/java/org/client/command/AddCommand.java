package org.client.command;

import org.codehaus.jettison.json.JSONObject;
import picocli.CommandLine;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "add", description = "add changes to staging area")
public class AddCommand extends BaseCommand{

    @CommandLine.Parameters(paramLabel = "files", description = "Specify one or more files")
    private final List<String> filesToAdd = new ArrayList<>();

    @Override
    public void run() {
        try{
            HttpURLConnection connection = serverConnection("add");

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("path", System.getProperty("user.dir"));
            jsonRequest.put("files", filesToAdd);
            sendToServer(jsonRequest, connection);

            getResponse(connection);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}