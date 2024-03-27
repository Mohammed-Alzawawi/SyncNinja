package org.syncninja.util;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class Neo4jSession {
    private static SessionFactory sessionFactory;

    private Neo4jSession() {
        long startTime = System.currentTimeMillis();
        Configuration configuration = new Configuration.Builder()
                .uri("bolt://localhost:7687")
                .credentials("neo4j", "12345678")
                .connectionPoolSize(10)
                .build();
        sessionFactory = new SessionFactory(configuration, "org.syncninja");
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
    }

    public static synchronized Session getSession() {

        if (sessionFactory == null) {
            new Neo4jSession();
        }
        return sessionFactory.openSession();
    }

    public static void closeSession(){
        sessionFactory.close();
    }

}
