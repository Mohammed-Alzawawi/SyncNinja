package org.syncninja;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.syncninja.command.MainCommand;
import org.syncninja.model.NinjaNode;
import org.syncninja.util.Neo4jSession;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MainCommand()).execute(args);
        System.exit(exitCode);
    }
}