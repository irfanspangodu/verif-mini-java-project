package com.verif.core.constant;

/**
 * Enumeration representing valid government-issued identification document types
 * accepted for profile verification within the Verif system.
 */
public enum IdType {

    /**
     * International travel passport document.
     */
    PASSPORT("Passport"),

    /**
     * Government National Identity Card.
     */
    NATIONAL_ID("National Identity Card"),

    /**
     * Official Driver's License permit.
     */
    DRIVERS_LICENSE("Driver's License");

    // Human-readable title for form selectors
    private final String label;

    /**
     * Constructs the IdType enum constant with a human-readable label.
     * 
     * @param label UI display text
     */
    IdType(String label) {
        this.label = label;
    }

    /**
     * Returns the human-readable label for rendering in JSF drop-down lists.
     * 
     * @return readable label
     */
    public String getLabel() {
        return label;
    }
}
