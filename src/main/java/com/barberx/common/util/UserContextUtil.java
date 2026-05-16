package com.barberx.common.util;

import com.barberx.common.context.UserContext;

/**
 * Thread-local utility to store and retrieve the current user's context.
 */
public final class UserContextUtil {

    private static final ThreadLocal<UserContext> contextHolder = new ThreadLocal<>();

    private UserContextUtil() {
        // Prevent instantiation
    }

    public static void setContext(UserContext context) {
        contextHolder.set(context);
    }

    public static UserContext getContext() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }

    /**
     * Helper method to get the current user's email, or 'SYSTEM' if unauthenticated.
     */
    public static String getCurrentUserEmail() {
        UserContext context = getContext();
        return (context != null && context.getEmail() != null) ? context.getEmail() : "SYSTEM";
    }
}
