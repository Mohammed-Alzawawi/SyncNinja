package org.client;

import org.client.command.MainCommand;
import picocli.CommandLine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Client {
    public static void main(String[] args) {
        try {
            URL url = new URL("http://localhost:8080/process");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            int exitCode = new CommandLine(new MainCommand()).execute(args);
            System.exit(exitCode);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}