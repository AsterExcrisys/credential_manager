package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.constants.GlobalConstants;
import com.asterexcrisys.acm.types.storage.OperatingSystem;
import com.asterexcrisys.acm.types.utility.Outcome;
import com.asterexcrisys.acm.types.utility.Result;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class GlobalUtility {

    private static final Logger LOGGER = Logger.getLogger(GlobalUtility.class.getName());

    private GlobalUtility() {
        // This class should not be instantiable
    }

    public static boolean isDebugEnabled() {
        return System.getProperty(GlobalConstants.DEBUG_PROPERTY, Boolean.FALSE.toString()).equalsIgnoreCase(Boolean.TRUE.toString());
    }

    public static OperatingSystem getOperatingSystem() {
        String name = System.getProperty("os.name", "unknown").toLowerCase();
        if (name.contains("windows")) {
            return OperatingSystem.WINDOWS;
        }
        if (name.contains("linux")) {
            return OperatingSystem.LINUX;
        }
        if (name.contains("mac")) {
            return OperatingSystem.MAC;
        }
        return OperatingSystem.UNKNOWN;
    }

    public static Optional<String> getSystemIdentifier() {
        ComputerSystem computerSystem = (new SystemInfo()).getHardware().getComputerSystem();
        String identifier = String.format(
                "%s-%s-%s",
                computerSystem.getSerialNumber(),
                computerSystem.getHardwareUUID(),
                getSystemUser()
        );
        return HashingUtility.hashMessage(identifier);
    }

    public static String getSystemUser() {
        return System.getProperty("user.name", "unknown");
    }

    public static String getWorkingDirectory() {
        Path workingDirectory = Paths.get(System.getProperty("user.home", System.getProperty("user.dir", "./")));
        return workingDirectory.resolve(".acm/").toAbsolutePath().toString();
    }

    public static String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(GlobalConstants.DATE_FORMAT);
        return now.format(formatter);
    }

    public static <T> Result<T, Exception> wrapExceptionOld(Callable<T> code) {
        try {
            return Result.success(code.call());
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public static <T> Outcome<T, Exception> wrapExceptionNew(Callable<T> code) {
        try {
            return Outcome.ofValue(code.call());
        } catch (Exception e) {
            return Outcome.ofError(e);
        }
    }

    public static <T> T ifThrows(Callable<T> code, T defaultValue) {
        try {
            return code.call();
        } catch (Exception e) {
            LOGGER.warning("Error executing callable: " + e.getMessage());
            return defaultValue;
        }
    }

    public static <T> void resizeList(List<T> list, int desiredSize) {
        if (list == null) {
            return;
        }
        if (desiredSize < 0) {
            desiredSize = 0;
        }
        int currentSize = list.size();
        if (currentSize > desiredSize) {
            list.subList(desiredSize, currentSize).clear();
        } else if (currentSize < desiredSize) {
            for (int i = currentSize; i < desiredSize; i++) {
                list.add(null);
            }
        }
    }

}