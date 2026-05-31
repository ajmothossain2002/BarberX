package com.barberx.core.shop.enums;

/**
 * Represents the verification lifecycle of a shop.
 * New shops start as PENDING; platform admins approve or reject them.
 */
public enum VerificationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
