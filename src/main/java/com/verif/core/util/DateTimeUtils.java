package com.verif.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class providing centralized date and time formatting methods
 * for user interfaces, audit timestamps, and notification bodies.
 */
public final class DateTimeUtils {

    // Standard human-friendly timestamp formatter: "MMM dd, yyyy HH:mm"
    private static final DateTimeFormatter FRIENDLY_DATETIME = 
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

    // Standard date-only formatter: "yyyy-MM-dd"
    private static final DateTimeFormatter STANDARD_DATE = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Private constructor to prevent instantiation.
     */
    private DateTimeUtils() {
        // Enforce non-instantiability
    }

    /**
     * Formats a LocalDateTime instance into a clear, friendly display string.
     * 
     * @param dateTime the LocalDateTime to format
     * @return Formatted string or "N/A" if null
     */
    public static String formatFriendly(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(FRIENDLY_DATETIME);
    }

    /**
     * Formats current timestamp as friendly string.
     * 
     * @return Formatted current date and time string
     */
    public static String nowFriendly() {
        return LocalDateTime.now().format(FRIENDLY_DATETIME);
    }
}
