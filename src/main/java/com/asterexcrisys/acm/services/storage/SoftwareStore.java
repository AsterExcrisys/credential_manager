package com.asterexcrisys.acm.services.storage;

import com.asterexcrisys.acm.types.storage.SoftwareStoreType;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class SoftwareStore implements Store<SecretKey> {

    private static final Logger LOGGER = Logger.getLogger(SoftwareStore.class.getName());

    private final Path storePath;
    private final char[] password;
    private final KeyStore store;

    public SoftwareStore(String fileName, String password, SoftwareStoreType type) throws Exception {
        storePath = Paths.get(String.format(
                "./data/%s.%s",
                Objects.requireNonNull(fileName),
                Objects.requireNonNull(type).name().toLowerCase()
        ));
        this.password = Objects.requireNonNull(password).toCharArray();
        store = KeyStore.getInstance(Objects.requireNonNull(type).name());
        loadStore();
    }

    public Optional<SecretKey> retrieve(String identifier) {
        try {
            Key key = store.getKey(identifier, password);
            if (key instanceof SecretKey data) {
                return Optional.of(data);
            }
            return Optional.empty();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            LOGGER.warning("Error retrieving key: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean save(String identifier, SecretKey data) {
        if (identifier == null || data == null || identifier.isBlank() || data.isDestroyed()) {
            return false;
        }
        SecretKeyEntry entry = new SecretKeyEntry(data);
        ProtectionParameter protection = new PasswordProtection(password);
        try {
            store.setEntry(identifier, entry, protection);
            saveStore();
            return true;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            LOGGER.warning("Error saving key: " + e.getMessage());
            return false;
        }
    }

    public boolean clear(String identifier) {
        try {
            store.deleteEntry(identifier);
            saveStore();
            return true;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            LOGGER.warning("Error clearing key: " + e.getMessage());
            return false;
        }
    }

    private void loadStore() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        Files.createDirectories(storePath.getParent());
        if (Files.exists(storePath)) {
            try (InputStream inputStream = Files.newInputStream(storePath, StandardOpenOption.READ)) {
                store.load(inputStream, password);
            }
        } else {
            store.load(null, password);
            saveStore();
        }
    }

    private void saveStore() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        try (OutputStream outputStream = Files.newOutputStream(storePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            store.store(outputStream, password);
        }
    }

}