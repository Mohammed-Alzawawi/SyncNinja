package org.client.command;

import org.codehaus.jettison.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class BaseCommand implements Runnable {
    protected HttpURLConnection serverConnection(String endpoint) throws IOException {
        URL url = new URL("http://localhost:8080/" + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setDoOutput(true);
        return connection;
    }

    protected void sendToServer(JSONObject jsonRequest, HttpURLConnection connection) throws Exception {
        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonRequest.toString().getBytes());
        }
    }

    protected void getResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            int character;
            while ((character = br.read()) != -1) {
                response.append((char) character);
            }
            System.out.println(response);
        }
    }
}