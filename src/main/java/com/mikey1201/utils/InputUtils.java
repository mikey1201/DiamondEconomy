package com.mikey1201.utils;

public class InputUtils {

    /**
     * Parses a double. Throws exception if invalid or <= 0.
     */
    public static double parsePositiveDouble(String input) throws IllegalArgumentException {
        if (input == null) throw new IllegalArgumentException("Input cannot be null.");
        try {
            double value = Double.parseDouble(input);
            if (value <= 0) throw new IllegalArgumentException("Amount must be positive.");
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + input);
        }
    }

    /**
     * Parses a double. Throws exception if invalid or < 0.
     * Allows 0.0 (Used by /eco set).
     */
    public static double parsePositiveDoubleAllowZero(String input) throws IllegalArgumentException {
        if (input == null) throw new IllegalArgumentException("Input cannot be null.");
        try {
            double value = Double.parseDouble(input);
            if (value < 0) throw new IllegalArgumentException("Amount must be positive.");
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + input);
        }
    }

    /**
     * Checks if a double is a whole number.
     * Used for item-based commands (deposit/withdraw).
     */
    public static void checkWholeNumber(double value) throws IllegalArgumentException {
        if (value % 1 != 0) {
            throw new IllegalArgumentException("You cannot use fractional items.");
        }
    }
}