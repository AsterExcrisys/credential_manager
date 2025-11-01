package com.asterexcrisys.acm.services.storage;

import com.asterexcrisys.acm.constants.EncryptionConstants;
import com.asterexcrisys.acm.constants.StorageConstants;
import com.asterexcrisys.acm.exceptions.AuthenticationException;
import com.asterexcrisys.acm.exceptions.PermissionException;
import com.asterexcrisys.acm.services.authentication.Authentication;
import com.asterexcrisys.acm.services.authentication.filters.ValidationStoreFilter;
import com.asterexcrisys.acm.utility.StorageUtility;
import de.fhg.iosb.iad.tpm.TpmEngine;
import de.fhg.iosb.iad.tpm.TpmEngine.TpmKey;
import de.fhg.iosb.iad.tpm.TpmEngine.TpmLoadedKey;
import de.fhg.iosb.iad.tpm.TpmEngine.TpmEngineException;
import de.fhg.iosb.iad.tpm.TpmEngineFactory;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class HardwareStore implements Store<SecretKey>, AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(HardwareStore.class.getName());

    private final Path storePath;
    private final TpmEngine engine;
    private final int policySession;

    // TODO: replace this with actually, properly fetched, known good PCR values
    public HardwareStore(String fileName) throws TpmEngineException, AuthenticationException, PermissionException, IOException {
        storePath = Paths.get(String.format(
                "./data/%s.tpm2",
                Objects.requireNonNull(fileName)
        ));
        engine = TpmEngineFactory.createPlatformInstance();
        Map<Integer, String> pcrValues = StorageUtility.registerPcrValues(engine)
                .orElseThrow(PermissionException::new);
        policySession = Authentication.authenticate(engine, new ValidationStoreFilter(pcrValues))
                .orElseThrow(AuthenticationException::new);
        loadStore();
    }

    public Optional<SecretKey> retrieve(String identifier) {
        try {
            if (!Files.exists(storePath)) {
                return Optional.empty();
            }
            byte[] encryptedData = Files.readAllBytes(storePath);
            TpmLoadedKey key = loadKey(identifier);
            byte[] decryptedData = engine.generateSharedSecret(key.handle, encryptedData);
            SecretKey data = new SecretKeySpec(decryptedData, 0, decryptedData.length, EncryptionConstants.KEY_GENERATION_ALGORITHM);
            engine.flushKey(key.handle);
            return Optional.of(data);
        } catch (IOException | TpmEngineException e) {
            LOGGER.warning("Error retrieving key: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean save(String identifier, SecretKey data) {
        try {
            TpmKey keyPair = loadOrCreateKey(identifier);
            byte[] decryptedData = data.getEncoded();
            PublicKey key = KeyFactory.getInstance(StorageConstants.KEY_GENERATION_ALGORITHM).generatePublic(new X509EncodedKeySpec(keyPair.outPublic));
            byte[] encryptedData = encrypt(key, decryptedData);
            saveStore(encryptedData);
            return true;
        } catch (IOException | TpmEngineException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.warning("Error saving key: " + e.getMessage());
            return false;
        }
    }

    public boolean clear(String identifier) {
        try {
            TpmLoadedKey key = loadKey(identifier);
            engine.flushKey(key.handle);
            saveStore(new byte[] {});
            return true;
        } catch (IOException | TpmEngineException e) {
            LOGGER.warning("Error clearing key: " + e.getMessage());
            return false;
        }
    }

    public void close() throws TpmEngineException {
        engine.flushKey(policySession);
        engine.shutdownTpm();
    }

    private void loadStore() throws IOException {
        Files.createDirectories(storePath.getParent());
        if (!Files.exists(storePath)) {
            saveStore(new byte[] {});
        }
    }

    private void saveStore(byte[] data) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(storePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            outputStream.write(data);
        }
    }

    private TpmLoadedKey loadKey(String identifier) throws TpmEngineException {
        return engine.loadSrk();
    }

    private TpmKey loadOrCreateKey(String identifier) throws TpmEngineException {
        return engine.createEphemeralDhKey(0);
    }

    private byte[] encrypt(PublicKey key, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(StorageConstants.ENCRYPTION_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

}