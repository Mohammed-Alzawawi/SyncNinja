package org.syncninja;
import com.sun.net.httpserver.HttpServer;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.syncninja.command.MainCommand;
import picocli.CommandLine;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

/*
  1- Open server (http)
  2- websocket server
  3- running java app
 */

public class Main {

    private static final int PORT = 8080;
    private static final int SERVER_TIMEOUT = 1 * 60 * 1000; // 10 minutes in milliseconds
    private static HttpServer server;
    private static Timer timer;

    public static void main(String[] args) {

        int port = 8080;

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/process", exchange -> {
                long startTime = System.currentTimeMillis();
                if ("POST".equals(exchange.getRequestMethod())) {
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new ShutdownTask(), SERVER_TIMEOUT);

                    // Read request body
                    StringBuilder requestBody = new StringBuilder();
                    try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody())) {
                        char[] buffer = new char[256];
                        int read;
                        while ((read = reader.read(buffer)) != -1) {
                            requestBody.append(buffer, 0, read);
                        }
                    }
                    JSONObject jsonObject;
                    String argsString, directory;
                    String id;
                    try {
                        jsonObject = new JSONObject(requestBody.toString());
                        argsString = jsonObject.getString("args");
                        directory = jsonObject.getString("directory");
                        id = jsonObject.getString("id");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    argsString += "-directory,"  + directory + ",";
                    argsString += "-id," + id;
                    new CommandLine(new MainCommand()).execute(argsString.split(","));
                    // Process the JSON request
                    System.out.println("Received JSON request: " + requestBody.toString());

                    // Send response
                    String response = OutputCollector.getString(id);
                    System.out.println(response);
                    OutputCollector.refresh(id);
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    long endTime = System.currentTimeMillis();
                    System.out.println(endTime - startTime);
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method not allowed
                }
            });

            timer = new Timer();
            timer.schedule(new ShutdownTask(), SERVER_TIMEOUT);


            server.start();
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   static class ShutdownTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("Server is shutting down due to inactivity.");
            server.stop(0); // Graceful shutdown
        }
    }

}