package com.asterexcrisys.acm.types.encryption;

import java.util.Arrays;

@SuppressWarnings("unused")
public enum VaultType {

    CREDENTIAL,
    TOKEN;

    public static String[] names() {
        return Arrays.stream(values()).map(VaultType::name).toArray(String[]::new);
    }

}