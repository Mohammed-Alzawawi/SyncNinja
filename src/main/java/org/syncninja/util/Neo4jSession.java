package org.syncninja.util;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class Neo4jSession {
    private static SessionFactory sessionFactory;
    private static Session session;

    private Neo4jSession() {
        Configuration configuration = new Configuration.Builder()
                .uri("bolt://localhost:7687")
                .credentials("neo4j", "12345678")
                .connectionPoolSize(10)
                .build();
        sessionFactory = new SessionFactory(configuration, "org.syncninja");
    }

    public static synchronized Session getSession() {

        if (sessionFactory == null) {
            new Neo4jSession();
        }
        if (session == null) {
            session = sessionFactory.openSession();
        }
        return session;
    }

    public static void closeSession(){
        session = null;
        sessionFactory.close();
    }
}
