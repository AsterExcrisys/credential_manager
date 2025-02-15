package org.pwman.types;

import javax.crypto.Cipher;

public enum EncryptorMode {

    ENCRYPT(Cipher.ENCRYPT_MODE),
    DECRYPT(Cipher.DECRYPT_MODE);

    private final int value;

    EncryptorMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}