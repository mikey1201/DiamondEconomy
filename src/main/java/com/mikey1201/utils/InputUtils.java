package com.mikey1201.utils;

public class InputUtils {

    /**
     * Digits with an optional decimal part. Deliberately strict: rejects
     * signs, whitespace, scientific notation ("1e5"), hex ("0x1p3"), and the
     * non-finite literals "NaN"/"Infinity" that Double.parseDouble accepts.
     */
    private static final String AMOUNT_PATTERN = "\\d+(\\.\\d+)?";

    /** Plain digits only. */
    private static final String WHOLE_NUMBER_PATTERN = "\\d+";

    /**
     * Parses a double. Throws exception if invalid or <= 0.
     */
    public static double parsePositiveDouble(String input) throws IllegalArgumentException {
        double value = parseValidatedDouble(input);
        if (value <= 0 || value == 0.0) {
            // note: "0" parses to 0.0 and is rejected here
            if (value <= 0) throw new IllegalArgumentException("Amount must be positive.");
        }
        if (value <= 0) throw new IllegalArgumentException("Amount must be positive.");
        return value;
    }

    /**
     * Parses a double. Throws exception if invalid or < 0.
     * Allows 0.0 (Used by /eco set).
     */
    public static double parsePositiveDoubleAllowZero(String input) throws IllegalArgumentException {
        double value = parseValidatedDouble(input);
        if (value < 0) throw new IllegalArgumentException("Amount must not be negative.");
        return value;
    }

    private static double parseValidatedDouble(String input) {
        if (input == null) throw new IllegalArgumentException("Input cannot be null.");
        if (!input.matches(AMOUNT_PATTERN)) {
            throw new IllegalArgumentException("Invalid number: " + input);
        }
        double value;
        try {
            value = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + input);
        }
        if (!Double.isFinite(value)) {
            // A string of hundreds of digits parses to Infinity
            throw new IllegalArgumentException("Number is too large: " + input);
        }
        return value;
    }

    /**
     * Parses a positive whole number into an int, throwing if the input is
     * not a plain positive integer or exceeds {@link Integer#MAX_VALUE}.
     *
     * Used for item-based commands (deposit/withdraw) so that amounts can be
     * safely used as item counts without silent (int) cast overflow.
     */
    public static int parsePositiveWholeInt(String input) throws IllegalArgumentException {
        if (input == null) throw new IllegalArgumentException("Input cannot be null.");
        if (!input.matches(WHOLE_NUMBER_PATTERN)) {
            throw new IllegalArgumentException("Invalid whole number: " + input);
        }
        final long value;
        try {
            value = Long.parseLong(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Number is too large: " + input);
        }
        if (value == 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Number is too large: " + input);
        }
        return (int) value;
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