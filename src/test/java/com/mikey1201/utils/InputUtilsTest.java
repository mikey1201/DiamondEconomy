package com.mikey1201.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class InputUtilsTest {

    // ---- Valid inputs ----

    @Test
    void parsesPlainIntegers() {
        assertEquals(10.0, InputUtils.parsePositiveDouble("10"));
    }

    @Test
    void parsesDecimals() {
        assertEquals(0.5, InputUtils.parsePositiveDouble("0.50"));
    }

    @Test
    void allowZeroAcceptsZero() {
        assertEquals(0.0, InputUtils.parsePositiveDoubleAllowZero("0"));
    }

    @Test
    void wholeIntParsesValidValues() {
        assertEquals(64, InputUtils.parsePositiveWholeInt("64"));
    }

    @Test
    void wholeIntAcceptsMaxInt() {
        assertEquals(Integer.MAX_VALUE, InputUtils.parsePositiveWholeInt("2147483647"));
    }

    // ---- Rejected inputs for parsePositiveDouble ----

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "Infinity", "-Infinity", "-5", "0", "0.0", "1e5", "0x1p3",
            " 10", "10 ", "+10", "1,5", "abc", "", "10.123.4", "5.", ".5"})
    void parsePositiveDoubleRejects(String input) {
        assertThrows(IllegalArgumentException.class, () -> InputUtils.parsePositiveDouble(input),
                "Should reject: '" + input + "'");
    }

    // ---- Rejected inputs for parsePositiveDoubleAllowZero ----

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "-0.1", "Infinity", "1e5", "-1", ""})
    void parsePositiveDoubleAllowZeroRejects(String input) {
        assertThrows(IllegalArgumentException.class, () -> InputUtils.parsePositiveDoubleAllowZero(input),
                "Should reject: '" + input + "'");
    }

    // ---- Rejected inputs for parsePositiveWholeInt ----

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "2147483648", "99999999999999999999",
            "1.5", "NaN", "1e3", "+5", " 7", "7 ", "abc", ""})
    void parsePositiveWholeIntRejects(String input) {
        assertThrows(IllegalArgumentException.class, () -> InputUtils.parsePositiveWholeInt(input),
                "Should reject: '" + input + "'");
    }

    // ---- The original NaN corruption bug must stay fixed ----

    @Test
    void nanCanNeverReachTheDatabase() {
        // Before the fix, "NaN" passed validation (NaN <= 0 is false) and
        // SQLite stored it as NULL, zeroing the balance.
        assertThrows(IllegalArgumentException.class, () -> InputUtils.parsePositiveDouble("NaN"));
        assertThrows(IllegalArgumentException.class, () -> InputUtils.parsePositiveDoubleAllowZero("NaN"));
        assertThrows(IllegalArgumentException.class, () -> InputUtils.parsePositiveWholeInt("NaN"));
    }

    // ---- checkWholeNumber still works ----

    @Test
    void checkWholeNumberAcceptsWholeValues() {
        assertDoesNotThrow(() -> InputUtils.checkWholeNumber(5.0));
    }

    @Test
    void checkWholeNumberRejectsFractions() {
        assertThrows(IllegalArgumentException.class, () -> InputUtils.checkWholeNumber(5.5));
    }
}
