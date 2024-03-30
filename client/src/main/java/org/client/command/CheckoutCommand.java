package org.client.command;

import org.codehaus.jettison.json.JSONObject;
import picocli.CommandLine;

import java.net.HttpURLConnection;

@CommandLine.Command(name = "checkout")
public class CheckoutCommand extends BaseCommand{

    @CommandLine.Parameters(paramLabel = "branch name", description = "name your branch")
    private String branchName;

    @CommandLine.Option(names = {"-b"}, paramLabel = "new branch name")
    private boolean isNewBranch;

    @Override
    public void run() {
        try{
            HttpURLConnection connection = serverConnection("checkout");

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("path", System.getProperty("user.dir"));
            jsonRequest.put("name", branchName);
            jsonRequest.put("isNewBranch", isNewBranch);
            sendToServer(jsonRequest, connection);

            getResponse(connection);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}