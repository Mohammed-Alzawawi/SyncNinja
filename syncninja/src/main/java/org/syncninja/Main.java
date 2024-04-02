package org.syncninja;

import com.sun.net.httpserver.HttpServer;
import org.syncninja.handler.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    private static final int PORT = 8080;
    private static final int SERVER_TIMEOUT = 10 * 60 * 1000; // 10 minutes in milliseconds
    private static HttpServer server;
    public static Timer timer;

    public static void main(String[] args) {
        int port = PORT;

        try {
            server = HttpServer.create(new InetSocketAddress(port), 1);

            server.createContext("/init", new InitHandler());
            server.createContext("/add", new AddHandler());
            server.createContext("/checkout", new CheckoutHandler());
            server.createContext("/commit", new CommitHandler());
            server.createContext("/restore", new RestoreHandler());
            server.createContext("/status", new StatusHandler());
            server.createContext("/unstage", new UnstageHandler());

            timer = new Timer();
            timer.schedule(new ShutdownTask(), SERVER_TIMEOUT);

            server.start();
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    static class ShutdownTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("Server is shutting down due to inactivity.");
            server.stop(0); // Graceful shutdown
        }
    }

    public static void restartTimer(){
        timer.cancel();
        timer = new Timer();
        timer.schedule(new ShutdownTask(), SERVER_TIMEOUT);
    }
}