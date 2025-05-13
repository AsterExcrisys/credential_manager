package com.asterexcrisys.acm.constants;

@SuppressWarnings("unused")
public final class Global {

    public static final String APPLICATION_NAME = "Aegis Credential Manager";
    public static final String APPLICATION_VERSION = "1.0.0";
    public static final String SHELL_PROMPT = String.format("%s@aegis> ", System.getProperty("user.dir", "unknown"));
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String UPCASE_BINDING = "upcase-word";
    public static final String DOWNCASE_BINDING = "downcase-word";

    private Global() {
        // This class should not be instantiated
    }

}