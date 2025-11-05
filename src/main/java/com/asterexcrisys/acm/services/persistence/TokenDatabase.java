package com.asterexcrisys.acm.services.persistence;

import com.asterexcrisys.acm.constants.PersistenceConstants;
import com.asterexcrisys.acm.exceptions.EncryptionException;
import com.asterexcrisys.acm.services.encryption.KeyEncryptor;
import com.asterexcrisys.acm.types.encryption.Token;
import com.asterexcrisys.acm.utility.DatabaseUtility;
import com.asterexcrisys.acm.utility.PathUtility;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class TokenDatabase implements Database {

    private static final Logger LOGGER = Logger.getLogger(TokenDatabase.class.getName());

    private final CoreDatabase database;

    public TokenDatabase(String vaultName, String masterKey) throws NullPointerException {
        database = new CoreDatabase(vaultName, PersistenceConstants.TOKEN_DATABASE, masterKey);
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
                "CREATE TABLE IF NOT EXISTS tokens (identifier TEXT PRIMARY KEY, key TEXT NOT NULL, expiration TEXT DEFAULT NULL, last_modification TEXT NOT NULL DEFAULT (STRFTIME('%Y-%m-%dT%H:%M:%fZ', 'now')));"
        ) == 0;
    }

    public boolean dropTable() {
        return database.executeUpdate("DROP TABLE IF EXISTS tokens;") == 0;
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
                && database.executeUpdate("INSERT OR REPLACE INTO tokens (identifier, key, expiration, last_modification) SELECT t.identifier, t.key, t.expiration, t.last_modification FROM file.tokens AS t;") == 0
                && database.executeUpdate("DETACH file;") == 0;
    }

    public boolean hasToken(String identifier, boolean isExpired) {
        if (identifier == null || identifier.isBlank()) {
            return false;
        }
        try (ResultSet resultSet = database.executeQuery(
                "SELECT t.identifier FROM tokens AS t WHERE t.identifier = ? AND (c.expiration IS NULL OR (c.expiration <= STRFTIME('%Y-%m-%dT%H:%M:%fZ', 'now')) = ?);",
                identifier,
                isExpired? Boolean.FALSE.toString():Boolean.TRUE.toString()
        )) {
            return resultSet != null && resultSet.next();
        } catch (SQLException e) {
            LOGGER.warning("Error retrieving token: " + e.getMessage());
            return false;
        }
    }

    public Optional<Token> getToken(String identifier, KeyEncryptor encryptor) {
        if (identifier == null || encryptor == null || identifier.isBlank()) {
            return Optional.empty();
        }
        try (ResultSet resultSet = database.executeQuery(
                "SELECT t.identifier, t.key, t.expiration, t.last_modification FROM tokens AS t WHERE t.identifier = ?;",
                identifier
        )) {
            if (resultSet == null || !resultSet.next()) {
                return Optional.empty();
            }
            Optional<String> key = encryptor.decrypt(resultSet.getString("key"));
            if (key.isEmpty()) {
                return Optional.empty();
            }
            Token token = new Token(
                    resultSet.getString("identifier"),
                    key.get(),
                    Instant.parse(resultSet.getString("last_modification"))
            );
            String expiration = resultSet.getString("expiration");
            if (expiration != null) {
                token.setExpiration(Instant.parse(expiration));
            }
            return Optional.of(token);
        } catch (SQLException | EncryptionException e) {
            LOGGER.warning("Error retrieving token: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<List<String>> getAllTokens() {
        try (ResultSet resultSet = database.executeQuery("SELECT t.identifier FROM tokens AS t;")) {
            List<String> tokens = new ArrayList<>();
            while (resultSet != null && resultSet.next()) {
                tokens.add(resultSet.getString("identifier"));
            }
            return Optional.of(tokens);
        } catch (SQLException e) {
            LOGGER.warning("Error retrieving all tokens: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean saveToken(Token token, KeyEncryptor encryptor) {
        if (token == null || encryptor == null) {
            return false;
        }
        Optional<String> key = token.getEncryptor().getEncryptedKey(encryptor);
        if (key.isEmpty()) {
            return false;
        }
        return database.executeUpdate(
                "INSERT OR REPLACE INTO tokens (identifier, key, expiration, last_modification) VALUES (?, ?, ?, ?);",
                token.getIdentifier(),
                key.get(),
                token.getExpiration().map(Instant::toString).orElse(null),
                token.getLastModification().toString()
        ) == 1;
    }

    public boolean removeToken(String identifier) {
        if (identifier == null) {
            return false;
        }
        return database.executeUpdate(
                "DELETE FROM tokens WHERE identifier = ?;",
                identifier
        ) == 1;
    }

    public boolean removeAllTokens() {
        Optional<List<String>> tokens = getAllTokens();
        if (tokens.isEmpty()) {
            return false;
        }
        return database.executeUpdate(
                "DELETE FROM tokens;"
        ) == tokens.get().size();
    }

    public void close() {
        database.close();
    }

}