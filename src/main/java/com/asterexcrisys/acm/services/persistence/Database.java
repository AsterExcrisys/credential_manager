package com.asterexcrisys.acm.services.persistence;

@SuppressWarnings("unused")
public sealed interface Database extends AutoCloseable permits CoreDatabase, VaultDatabase, CredentialDatabase {

    boolean connect();

    void disconnect();

}