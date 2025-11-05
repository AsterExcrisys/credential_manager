package com.asterexcrisys.acm.constants;

@SuppressWarnings("unused")
public final class PersistenceConstants {

    public static final String JDBC_DRIVER = "org.sqlite.JDBC";
    public static final String VAULT_DATABASE = "vaults";
    public static final String CREDENTIAL_DATABASE = "credentials";
    public static final String TOKEN_DATABASE = "tokens";

    private PersistenceConstants() {
        // This class should not be instantiable
    }

}