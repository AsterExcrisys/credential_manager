package com.asterexcrisys.acm.services.persistence;

import com.asterexcrisys.acm.services.encryption.KeyEncryptor;
import com.asterexcrisys.acm.types.encryption.Credential;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class CredentialDatabase implements Database {

    private static final Logger LOGGER = Logger.getLogger(CredentialDatabase.class.getName());
    private final CoreDatabase database;

    public CredentialDatabase(String vaultName, String masterKey) throws NullPointerException {
        database = new CoreDatabase(vaultName, "credentials", masterKey);
    }

    public boolean connect() {
        return database.connect();
    }

    public void disconnect() {
        database.disconnect();
    }

    public boolean createTable() {
        return database.executeUpdate(
                "CREATE TABLE IF NOT EXISTS credentials (platform TEXT PRIMARY KEY, username TEXT, password TEXT, key TEXT);"
        ) == 0;
    }

    public Optional<Credential> getCredential(String platform, KeyEncryptor encryptor) {
        if (platform == null || encryptor == null) {
            return Optional.empty();
        }
        try (ResultSet resultSet = database.executeQuery(
                "SELECT c.platform, c.username, c.password, c.key FROM credentials AS c WHERE platform = ?;",
                platform
        )) {
            if (resultSet == null || !resultSet.next()) {
                return Optional.empty();
            }
            Optional<String> key = encryptor.decrypt(resultSet.getString("key"));
            if (key.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Credential(
                    key.get(),
                    resultSet.getString("platform"),
                    resultSet.getString("username"),
                    resultSet.getString("password")
            ));
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving credential: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<List<String>> getAllCredentials() {
        try (ResultSet resultSet = database.executeQuery("SELECT c.platform FROM credentials AS c;")) {
            List<String> credentials = new ArrayList<>();
            while (resultSet != null && resultSet.next()) {
                credentials.add(resultSet.getString("platform"));
            }
            return Optional.of(credentials);
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving all credentials: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean saveCredential(Credential credential, KeyEncryptor encryptor) {
        if (credential == null || encryptor == null) {
            return false;
        }
        Optional<String> key = credential.getEncryptor().getEncryptedKey(encryptor);
        if (key.isEmpty()) {
            return false;
        }
        return database.executeUpdate(
                "INSERT OR REPLACE INTO credentials (key, platform, username, password) VALUES (?, ?, ?, ?);",
                key.get(),
                credential.getPlatform(),
                credential.getEncryptedUsername(),
                credential.getEncryptedPassword()
        ) == 1;
    }

    public boolean removeCredential(String platform) {
        if (platform == null) {
            return false;
        }
        return database.executeUpdate(
                "DELETE FROM credentials WHERE platform = ?;",
                platform
        ) == 1;
    }

    public void close() {
        database.close();
    }

}