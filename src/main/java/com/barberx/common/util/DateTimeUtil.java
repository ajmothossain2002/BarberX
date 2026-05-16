package com.barberx.common.util;

import java.time.LocalDateTime;

/**
 * Utility class to provide consistent date and time functions across the application.
 */
public final class DateTimeUtil {

    private DateTimeUtil() {
        // Prevent instantiation
    }

    /**
     * Gets the current local date and time.
     *
     * @return LocalDateTime representing current time.
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
