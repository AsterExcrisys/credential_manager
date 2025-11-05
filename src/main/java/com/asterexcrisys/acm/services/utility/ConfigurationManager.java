package com.asterexcrisys.acm.services.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

@SuppressWarnings("unused")
public class ConfigurationManager {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationManager.class.getName());

    private final Preferences configuration;

    public ConfigurationManager() {
        configuration = Preferences.userNodeForPackage(ConfigurationManager.class);
    }

    public Path getConfigurationPath() {
        return Paths.get(configuration.absolutePath());
    }

    public void addListener(PreferenceChangeListener listener) {
        configuration.addPreferenceChangeListener(Objects.requireNonNull(listener));
    }

    public void removeListener(PreferenceChangeListener listener) {
        configuration.removePreferenceChangeListener(Objects.requireNonNull(listener));
    }

    public boolean contains(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        return configuration.get(key, null) != null;
    }

    public Optional<String> get(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        String value = configuration.get(key, null);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    public String get(String key, String defaultValue) {
        if (key == null || key.isBlank()) {
            return defaultValue;
        }
        return configuration.get(key, defaultValue);
    }

    public boolean put(String key, String value) {
        if (key == null || value == null || key.isBlank() || value.isBlank()) {
            return false;
        }
        try {
            configuration.put(key, value);
            configuration.flush();
            return true;
        } catch (BackingStoreException e) {
            LOGGER.warning("Error persisting configuration insertion/update: " + e.getMessage());
            return false;
        }
    }

    public boolean remove(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        try {
            configuration.remove(key);
            configuration.flush();
            return true;
        } catch (BackingStoreException e) {
            LOGGER.warning("Error persisting configuration removal: " + e.getMessage());
            return false;
        }
    }

    public boolean clear() {
        try {
            configuration.clear();
            configuration.flush();
            return true;
        } catch (BackingStoreException e) {
            LOGGER.warning("Error persisting configuration clear: " + e.getMessage());
            return false;
        }
    }

    public boolean importConfiguration(InputStream input) {
        if (input == null) {
            return false;
        }
        try {
            Preferences.importPreferences(input);
            return true;
        } catch (IOException | InvalidPreferencesFormatException e) {
            LOGGER.warning("Error importing configuration: " + e.getMessage());
            return false;
        }
    }

    public boolean exportConfiguration(OutputStream output) {
        if (output == null) {
            return false;
        }
        try {
            configuration.exportNode(output);
            return true;
        } catch (IOException | BackingStoreException e) {
            LOGGER.warning("Error exporting configuration: " + e.getMessage());
            return false;
        }
    }

}