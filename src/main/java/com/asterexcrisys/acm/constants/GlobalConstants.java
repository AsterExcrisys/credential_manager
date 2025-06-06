package com.asterexcrisys.acm.constants;

@SuppressWarnings("unused")
public final class GlobalConstants {

    public static final String APPLICATION_NAME = "Aegis Credential Manager";
    public static final String APPLICATION_VERSION = "1.0.0";
    public static final String ROOT_LOGGER = "";
    public static final String SHELL_PROMPT = String.format("%s@aegis> ", System.getProperty("user.name", "unknown"));
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TERMINAL_TYPE = System.getenv().getOrDefault("TERM", "ANSI");
    public static final String UPCASE_BINDING = "upcase-word";
    public static final String DOWNCASE_BINDING = "downcase-word";

    private GlobalConstants() {
        // This class should not be instantiated
    }

}