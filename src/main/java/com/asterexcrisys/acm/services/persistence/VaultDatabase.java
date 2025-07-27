package com.asterexcrisys.acm.services.persistence;

import com.asterexcrisys.acm.constants.PersistenceConstants;
import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.exceptions.HashingException;
import com.asterexcrisys.acm.utility.DatabaseUtility;
import com.asterexcrisys.acm.utility.HashingUtility;
import com.asterexcrisys.acm.types.encryption.Vault;
import com.asterexcrisys.acm.utility.PathUtility;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class VaultDatabase implements Database {

    private static final Logger LOGGER = Logger.getLogger(VaultDatabase.class.getName());

    private final CoreDatabase database;

    public VaultDatabase(String masterKey) throws NullPointerException {
        database = new CoreDatabase(PersistenceConstants.VAULT_DATABASE, masterKey);
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
                "CREATE TABLE IF NOT EXISTS vaults (name TEXT PRIMARY KEY, password TEXT, salt TEXT);"
        ) == 0;
    }

    public boolean dropTable() {
        return database.executeUpdate("DROP TABLE IF EXISTS vaults;") == 0;
    }

    public boolean backupTo(Path backupFile) {
        Optional<Path> databaseFile = database.getDatabasePath();
        if (databaseFile.isEmpty()) {
            return false;
        }
        return DatabaseUtility.backupTo(databaseFile.get(), backupFile);
    }

    public boolean restoreFrom(Path backupFile) {
        Optional<Path> databaseFile = database.getDatabasePath();
        if (databaseFile.isEmpty()) {
            return false;
        }
        return DatabaseUtility.restoreFrom(databaseFile.get(), backupFile);
    }

    public boolean mergeFrom(Path file, String masterKey) {
        if (PathUtility.isFileInDirectory(Paths.get("./data/"), file)) {
            return false;
        }
        if (!Files.isReadable(file)) {
            return false;
        }
        if (masterKey == null || masterKey.isBlank()) {
            return false;
        }
        return database.executeUpdate("ATTACH ? AS file KEY ?;", file.toAbsolutePath().toString(), masterKey) == 0
                && database.executeUpdate("INSERT OR REPLACE INTO vaults (name, password, salt) SELECT v.name, v.salt FROM vaults AS v;") == 0
                && database.executeUpdate("DETACH file;") == 0;
    }

    public Optional<Vault> getVault(String name, String password) {
        if (name == null) {
            return Optional.empty();
        }
        try (ResultSet resultSet = database.executeQuery(
                "SELECT v.name, v.password, v.salt FROM vaults AS v WHERE v.name = ?;",
                name
        )) {
            if (resultSet == null || !resultSet.next()) {
                return Optional.empty();
            }
            if (!HashingUtility.verifyPassword(password, resultSet.getString("password"))) {
                return Optional.empty();
            }
            return Optional.of(new Vault(
                    resultSet.getString("salt"),
                    resultSet.getString("password"),
                    resultSet.getString("name"),
                    password
            ));
        } catch (SQLException | NoSuchAlgorithmException | DerivationException | HashingException e) {
            LOGGER.warning("Error retrieving vault: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<List<String>> getAllVaults() {
        try (ResultSet resultSet = database.executeQuery(
                "SELECT v.name FROM vaults AS v;"
        )) {
            List<String> vaults = new ArrayList<>();
            while (resultSet != null && resultSet.next()) {
                vaults.add(resultSet.getString("name"));
            }
            return Optional.of(vaults);
        } catch (SQLException e) {
            LOGGER.warning("Error retrieving all vaults: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean saveVault(Vault vault) {
        if (vault == null) {
            return false;
        }
        return database.executeUpdate(
                "INSERT OR REPLACE INTO vaults (name, password, salt) VALUES (?, ?, ?);",
                vault.getName(),
                vault.getHashedPassword(),
                vault.getEncryptor().getSealedSalt()
        ) == 1;
    }

    public boolean removeVault(String name, String password) {
        if (name == null || password == null || name.isBlank() || password.isBlank()) {
            return false;
        }
        if (getVault(name, password).isEmpty()) {
            return false;
        }
        return database.executeUpdate(
                "DELETE FROM vaults WHERE name = ?;",
                name
        ) == 1;
    }

    public void close() {
        database.close();
    }

}