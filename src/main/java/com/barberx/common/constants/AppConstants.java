package com.barberx.common.constants;

/**
 * Application-wide constants for reuse across modules.
 */
public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // API versioning
    public static final String API_V1 = "/api/v1";

    // Default roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_BARBER = "BARBER";

    // Auth headers
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
}
