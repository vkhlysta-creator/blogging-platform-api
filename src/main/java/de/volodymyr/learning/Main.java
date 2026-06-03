package de.volodymyr.learning;


import de.volodymyr.learning.repository.PostgresSQLRepository;
import de.volodymyr.learning.server.BlogApiServer;
import de.volodymyr.learning.service.BlogService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        String dbUrl = getEnvOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/blog_db");
        String dbUser = getEnvOrDefault("DB_USER", "postgres");
        String dbPassword = System.getenv("DB_PASSWORD");
        final Connection connection;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down application...");
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.close();
                        System.out.println("Database connection closed.");
                    }
                } catch (SQLException e) {
                    System.out.println("Error closing database connection: " + e.getMessage());
                }
            }));

            PostgresSQLRepository repository = new PostgresSQLRepository(connection);
            BlogService service = new BlogService(repository);
            BlogApiServer server = new BlogApiServer(service);


            server.start(8080);
            System.out.println("Server is running on http://localhost:8080");


        } catch (SQLException | IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}