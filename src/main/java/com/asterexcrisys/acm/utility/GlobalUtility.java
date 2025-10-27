package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.constants.GlobalConstants;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@SuppressWarnings("unused")
public final class GlobalUtility {

    private GlobalUtility() {
        // This class should not be instantiable
    }

    public static boolean isDebugEnabled() {
        return System.getProperty(GlobalConstants.DEBUG_PROPERTY, Boolean.FALSE.toString()).equalsIgnoreCase(Boolean.TRUE.toString());
    }

    public static String getUniqueIdentifier() {
        ComputerSystem computerSystem = (new SystemInfo()).getHardware().getComputerSystem();
        String identifier = computerSystem.getSerialNumber().concat(computerSystem.getHardwareUUID());
        return Base64.getEncoder().encodeToString(identifier.getBytes());
    }

    public static String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(GlobalConstants.DATE_FORMAT);
        return now.format(formatter);
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