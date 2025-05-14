package com.asterexcrisys.acm.constants;

@SuppressWarnings("unused")
public final class Persistence {

    public static final String JDBC_DRIVER = "org.sqlite.JDBC";
    public static final String VAULT_DATABASE = "vaults";
    public static final String CREDENTIAL_DATABASE = "credentials";

    private Persistence() {
        // This class should not be instantiable
    }

}