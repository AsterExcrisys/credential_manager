package com.asterexcrisys.acm.types.storage;

@SuppressWarnings("unused")
public enum SoftwareStoreType {

    JCEKS("jceks"),
    BCFKS("bcfks"),
    PKCS12("pfx");

    private final String extension;

    SoftwareStoreType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

}