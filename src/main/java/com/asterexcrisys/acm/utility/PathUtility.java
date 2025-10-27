package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.services.utility.DeletePathVisitor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class PathUtility {

    private static final Logger LOGGER = Logger.getLogger(PathUtility.class.getName());

    private PathUtility() {
        // This class should not be instantiable
    }

    public static boolean deleteRecursively(Path path) {
        if (path == null) {
            return false;
        }
        if (!Files.exists(path)) {
            return false;
        }
        try {
            if (Files.isRegularFile(path)) {
                Files.delete(path);
                return true;
            }
            if (Files.isDirectory(path)) {
                Files.walkFileTree(path, new DeletePathVisitor());
                return true;
            }
            return false;
        } catch (IOException e) {
            LOGGER.warning("Error recursively deleting path: " + e.getMessage());
            return false;
        }
    }

    public static boolean isFileInDirectory(Path directory, Path file) {
        if (directory == null || file == null) {
            return false;
        }
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return false;
        }
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return false;
        }
        return file.toAbsolutePath().startsWith(directory.toAbsolutePath());
    }

}