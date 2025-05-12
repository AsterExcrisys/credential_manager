package com.asterexcrisys.acm.services.persistence;

import com.asterexcrisys.acm.services.Utility;
import com.asterexcrisys.acm.types.encryption.Vault;
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
        database = new CoreDatabase("vaults", masterKey);
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

    public Optional<Vault> getVault(String name, String password) {
        if (name == null) {
            return Optional.empty();
        }
        Optional<String> hash = Utility.hash(password);
        if (hash.isEmpty()) {
            return Optional.empty();
        }
        try (ResultSet resultSet = database.executeQuery(
                "SELECT v.name, v.salt FROM vaults AS v WHERE name = ? AND password = ?;",
                name,
                hash.get()
        )) {
            if (resultSet == null || !resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(new Vault(
                    resultSet.getString("name"),
                    password,
                    resultSet.getString("salt")
            ));
        } catch (SQLException | NoSuchAlgorithmException e) {
            LOGGER.severe("Error retrieving vault: " + e.getMessage());
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
            LOGGER.severe("Error retrieving all vaults: " + e.getMessage());
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
        if (name == null) {
            return false;
        }
        Optional<String> hash = Utility.hash(password);
        if (hash.isEmpty()) {
            return false;
        }
        return database.executeUpdate(
                "DELETE FROM vaults WHERE name = ? AND password = ?;",
                name,
                hash.get()
        ) == 1;
    }

    public void close() {
        database.close();
    }

}