package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.constants.GlobalConstants;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

}