package com.mikey1201.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateCheckerTest {

    @Test
    void numericComparisonNotLexicographic() {
        // String comparison would rank "1.2.2" newer than "1.10"
        assertTrue(UpdateChecker.compareVersions("1.2.2", "1.10") < 0);
        assertTrue(UpdateChecker.compareVersions("1.10", "1.2.2") > 0);
    }

    @Test
    void equalVersionsCompareEqual() {
        assertEquals(0, UpdateChecker.compareVersions("1.2.2", "1.2.2"));
    }

    @Test
    void differentSegmentCounts() {
        assertTrue(UpdateChecker.compareVersions("1.2", "1.2.1") < 0);
        assertTrue(UpdateChecker.compareVersions("1.2.1", "1.2") > 0);
        assertEquals(0, UpdateChecker.compareVersions("1.2.0", "1.2"));
    }

    @Test
    void nonNumericSuffixesTolerated() {
        assertTrue(UpdateChecker.compareVersions("1.2.2", "1.10-beta") < 0);
        assertEquals(0, UpdateChecker.compareVersions("1.2.2", "1.2.2-SNAPSHOT"));
    }
}