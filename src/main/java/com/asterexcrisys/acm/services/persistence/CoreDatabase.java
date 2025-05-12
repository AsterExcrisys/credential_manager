package com.asterexcrisys.acm.services.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Objects;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class CoreDatabase implements Database {

    private static final Logger LOGGER = Logger.getLogger(CoreDatabase.class.getName());
    private final Path databasePath;
    private final String masterKey;
    private Connection connection;

    public CoreDatabase(String databaseName, String masterKey) throws NullPointerException {
        databasePath = Paths.get(String.format("./data/%s.db", Objects.requireNonNull(databaseName)));
        this.masterKey = Objects.requireNonNull(masterKey);
        connection = null;
    }

    public CoreDatabase(String vaultName, String databaseName, String masterKey) throws NullPointerException {
        databasePath = Paths.get(String.format("./data/%s/%s.db", Objects.requireNonNull(vaultName), Objects.requireNonNull(databaseName)));
        this.masterKey = Objects.requireNonNull(masterKey);
        connection = null;
    }

    public boolean connect() {
        try {
            Files.createDirectories(databasePath.getParent());
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", databasePath.toAbsolutePath()));
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(String.format("PRAGMA key = '%s';", masterKey));
            }
            return true;
        } catch (Exception e) {
            LOGGER.severe("Error connecting to the database: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.severe("Error closing the connection: " + e.getMessage());
        }
    }

    public ResultSet executeQuery(String query, String... parameters) {
        if (connection == null || query == null || query.isBlank()) {
            return null;
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setString(i + 1, parameters[i]);
            }
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            LOGGER.severe("Error executing query: " + e.getMessage());
            return null;
        }
    }

    public int executeUpdate(String query, String... parameters) {
        if (connection == null || query == null || query.isBlank()) {
            return -1;
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setString(i + 1, parameters[i]);
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Error executing update: " + e.getMessage());
            return -1;
        }
    }

    public void close() {
        disconnect();
    }

}