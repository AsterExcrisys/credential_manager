package com.asterexcrisys.acm.types.encryption;

import javax.crypto.Cipher;

@SuppressWarnings("unused")
public enum CipherMode {

    ENCRYPT(Cipher.ENCRYPT_MODE),
    DECRYPT(Cipher.DECRYPT_MODE);

    private final int value;

    CipherMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}