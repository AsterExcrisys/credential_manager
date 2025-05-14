package com.asterexcrisys.acm.services.persistence;

import com.asterexcrisys.acm.constants.Persistence;
import org.sqlite.mc.SQLiteMCChacha20Config;
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

    public CoreDatabase(String fileName, String masterKey) throws NullPointerException {
        databasePath = Paths.get(String.format("./data/%s.db", Objects.requireNonNull(fileName)));
        this.masterKey = Objects.requireNonNull(masterKey);
        connection = null;
    }

    public CoreDatabase(String directoryName, String fileName, String masterKey) throws NullPointerException {
        databasePath = Paths.get(String.format("./data/%s/%s.db", Objects.requireNonNull(directoryName), Objects.requireNonNull(fileName)));
        this.masterKey = Objects.requireNonNull(masterKey);
        connection = null;
    }

    public boolean connect() {
        if (connection != null) {
            return false;
        }
        try {
            Files.createDirectories(databasePath.getParent());
            Class.forName(Persistence.JDBC_DRIVER);
            SQLiteMCChacha20Config configuration = SQLiteMCChacha20Config.getDefault();
            configuration.withKey(masterKey);
            connection = DriverManager.getConnection(
                    String.format("jdbc:sqlite:%s", databasePath.toAbsolutePath()),
                    configuration.build().toProperties()
            );
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

    public boolean beginTransaction() {
        if (connection == null) {
            return false;
        }
        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            return true;
        } catch (SQLException e) {
            LOGGER.severe("Error beginning the transaction: " + e.getMessage());
            return false;
        }
    }

    public boolean endTransaction(boolean shouldCommit) {
        if (connection == null) {
            return false;
        }
        try {
            if (shouldCommit) {
                connection.commit();
            } else {
                connection.rollback();
            }
            return true;
        } catch (SQLException e) {
            LOGGER.severe("Error ending the transaction: " + e.getMessage());
            return false;
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
        connection = null;
    }

}