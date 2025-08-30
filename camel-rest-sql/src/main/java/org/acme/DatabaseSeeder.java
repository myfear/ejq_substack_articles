// src/main/java/org/acme/DatabaseSeeder.java
package org.acme;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Startup
@ApplicationScoped
public class DatabaseSeeder {

    @Inject
    DataSource dataSource; // Inject the datasource configured by Quarkus

    @PostConstruct
    void setupDb() {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {

            // Drop table if it exists and create a new one
            statement.execute("DROP TABLE IF EXISTS users;");
            statement.execute("""
                        CREATE TABLE users (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          city VARCHAR(255)
                        );
                    """);

            // Insert initial data
            statement.execute("""
                        INSERT INTO users (name, city) VALUES
                          ('Alice', 'Amsterdam'),
                          ('Bob', 'Berlin'),
                          ('Carol', 'Copenhagen');
                    """);

        } catch (Exception e) {
            // In a real application, you'd handle this exception properly
            throw new RuntimeException("Could not initialize database", e);
        }
    }
}