package org.client.command;

import org.codehaus.jettison.json.JSONObject;
import picocli.CommandLine;

import java.net.HttpURLConnection;

@CommandLine.Command(name = "init", description = "Initialize directory")
public class InitCommand extends BaseCommand {

    @Override
    public void run() {
        try {
            HttpURLConnection connection = serverConnection("init");

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("path", System.getProperty("user.dir"));
            sendToServer(jsonRequest, connection);

            getResponse(connection);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}