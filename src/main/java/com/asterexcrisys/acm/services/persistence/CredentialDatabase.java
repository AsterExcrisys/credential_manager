package com.asterexcrisys.acm.services.persistence;

import com.asterexcrisys.acm.constants.PersistenceConstants;
import com.asterexcrisys.acm.exceptions.EncryptionException;
import com.asterexcrisys.acm.services.encryption.KeyEncryptor;
import com.asterexcrisys.acm.types.encryption.Credential;
import com.asterexcrisys.acm.utility.DatabaseUtility;
import com.asterexcrisys.acm.utility.PathUtility;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        database = new CoreDatabase(vaultName, PersistenceConstants.CREDENTIAL_DATABASE, masterKey);
    }

    public Optional<Path> getDatabasePath() {
        return database.getDatabasePath();
    }

    public boolean connect() {
        return database.connect();
    }

    public void disconnect() {
        database.disconnect();
    }

    public boolean createTable() {
        return database.executeUpdate(
                "CREATE TABLE IF NOT EXISTS credentials (platform TEXT PRIMARY KEY, username TEXT NOT NULL, password TEXT NOT NULL, key TEXT NOT NULL);"
        ) == 0;
    }

    public boolean dropTable() {
        return database.executeUpdate("DROP TABLE IF EXISTS credentials;") == 0;
    }

    public boolean backupTo(Path backupFile) {
        if (backupFile == null) {
            return false;
        }
        Optional<Path> databaseFile = database.getDatabasePath();
        if (databaseFile.isEmpty()) {
            return false;
        }
        return DatabaseUtility.backupTo(databaseFile.get(), backupFile);
    }

    public boolean restoreFrom(Path backupFile) {
        if (backupFile == null) {
            return false;
        }
        Optional<Path> databaseFile = database.getDatabasePath();
        if (databaseFile.isEmpty()) {
            return false;
        }
        return DatabaseUtility.restoreFrom(databaseFile.get(), backupFile);
    }

    public boolean mergeWith(Path file, String masterKey) {
        if (file == null || masterKey == null || masterKey.isBlank()) {
            return false;
        }
        if (PathUtility.isFileInDirectory(Paths.get("./data/"), file)) {
            return false;
        }
        if (!Files.isReadable(file)) {
            return false;
        }
        return database.executeUpdate("ATTACH ? AS file KEY ?;", file.toAbsolutePath().toString(), masterKey) == 0
                && database.executeUpdate("INSERT OR REPLACE INTO credentials (platform, username, password, key) SELECT c.platform, c.username, c.password, c.key FROM file.credentials AS c;") == 0
                && database.executeUpdate("DETACH file;") == 0;
    }

    public boolean hasCredential(String platform) {
        if (platform == null || platform.isBlank()) {
            return false;
        }
        try (ResultSet resultSet = database.executeQuery(
                "SELECT c.platform FROM credentials AS c WHERE c.platform = ?;",
                platform
        )) {
            return resultSet !=null && resultSet.next();
        } catch (SQLException e) {
            LOGGER.warning("Error retrieving credential: " + e.getMessage());
            return false;
        }
    }

    public Optional<Credential> getCredential(String platform, KeyEncryptor encryptor) {
        if (platform == null || encryptor == null || platform.isBlank()) {
            return Optional.empty();
        }
        try (ResultSet resultSet = database.executeQuery(
                "SELECT c.platform, c.username, c.password, c.key FROM credentials AS c WHERE c.platform = ?;",
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
        } catch (SQLException | EncryptionException e) {
            LOGGER.warning("Error retrieving credential: " + e.getMessage());
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
            LOGGER.warning("Error retrieving all credentials: " + e.getMessage());
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
                "INSERT OR REPLACE INTO credentials (platform, username, password, key) VALUES (?, ?, ?, ?);",
                credential.getPlatform(),
                credential.getEncryptedUsername(),
                credential.getEncryptedPassword(),
                key.get()
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

    public boolean removeAllCredentials() {
        Optional<List<String>> credentials = getAllCredentials();
        if (credentials.isEmpty()) {
            return false;
        }
        return database.executeUpdate(
                "DELETE FROM credentials;"
        ) == credentials.get().size();
    }

    public void close() {
        database.close();
    }

}