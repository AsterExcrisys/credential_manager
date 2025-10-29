package com.asterexcrisys.acm.constants;

import com.asterexcrisys.acm.utility.GlobalUtility;

@SuppressWarnings("unused")
public final class GlobalConstants {

    public static final String APPLICATION_NAME = "Aegis Credential Manager";
    public static final String APPLICATION_VERSION = "1.0.0";
    public static final String WORKING_DIRECTORY = GlobalUtility.getWorkingDirectory();
    public static final String ROOT_LOGGER = "";
    public static final String DEBUG_PROPERTY = "acm.debug";
    public static final String SHELL_PROMPT = String.format("%s@aegis> ", GlobalUtility.getSystemUser());
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TERMINAL_TYPE = System.getenv().getOrDefault("TERM", "ANSI");
    public static final String UPCASE_BINDING = "upcase-word";
    public static final String DOWNCASE_BINDING = "downcase-word";

    private GlobalConstants() {
        // This class should not be instantiated
    }

}