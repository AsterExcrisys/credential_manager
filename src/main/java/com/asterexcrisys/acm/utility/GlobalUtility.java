package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.constants.GlobalConstants;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings("unused")
public final class GlobalUtility {

    private GlobalUtility() {
        // This class should not be instantiable
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