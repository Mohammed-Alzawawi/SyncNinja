package org.client.command;

import org.codehaus.jettison.json.JSONObject;
import picocli.CommandLine;

import java.net.HttpURLConnection;

@CommandLine.Command(name = "commit")
public class CommitCommand extends BaseCommand {

    @CommandLine.Option(names = {"-m"}, paramLabel = "message", description = "Enter a message for the commit", required = true)
    private String message;

    @Override
    public void run() {
        try{
            HttpURLConnection connection = serverConnection("commit");

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("path", System.getProperty("user.dir"));
            jsonRequest.put("message", message);
            sendToServer(jsonRequest, connection);

            getResponse(connection);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}