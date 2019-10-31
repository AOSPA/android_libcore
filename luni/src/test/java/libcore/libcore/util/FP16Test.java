/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.libcore.util;

import static libcore.util.FP16.*;
import libcore.util.FP16;

import junit.framework.TestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FP16Test extends TestCase {
    private static void assertShortEquals(short a, short b) {
        assertEquals((long) (a & 0xffff), (long) (b & 0xffff));
    }

    private static void assertShortEquals(int a, short b) {
        assertEquals((long) (a & 0xffff), (long) (b & 0xffff));
    }

    public void testSingleToHalf() {
        // Zeroes, NaN and infinities
        assertShortEquals(POSITIVE_ZERO, toHalf(0.0f));
        assertShortEquals(NEGATIVE_ZERO, toHalf(-0.0f));
        assertShortEquals(NaN, toHalf(Float.NaN));
        assertShortEquals(POSITIVE_INFINITY, toHalf(Float.POSITIVE_INFINITY));
        assertShortEquals(NEGATIVE_INFINITY, toHalf(Float.NEGATIVE_INFINITY));
        // Known values
        assertShortEquals(0x3c01, toHalf(1.0009765625f));
        assertShortEquals(0xc000, toHalf(-2.0f));
        assertShortEquals(0x0400, toHalf(6.10352e-5f));
        assertShortEquals(0x7bff, toHalf(65504.0f));
        assertShortEquals(0x3555, toHalf(1.0f / 3.0f));
        // Subnormals
        assertShortEquals(0x03ff, toHalf(6.09756e-5f));
        assertShortEquals(MIN_VALUE, toHalf(5.96046e-8f));
        assertShortEquals(0x83ff, toHalf(-6.09756e-5f));
        assertShortEquals(0x8001, toHalf(-5.96046e-8f));
        // Subnormals (flushed to +/-0)
        assertShortEquals(POSITIVE_ZERO, toHalf(5.96046e-9f));
        assertShortEquals(NEGATIVE_ZERO, toHalf(-5.96046e-9f));
        // Test for values that overflow the mantissa bits into exp bits
        assertShortEquals(0x1000, toHalf(Float.intBitsToFloat(0x39fff000)));
        assertShortEquals(0x0400, toHalf(Float.intBitsToFloat(0x387fe000)));
        // Floats with absolute value above +/-65519 are rounded to +/-inf
        // when using round-to-even
        assertShortEquals(0x7bff, toHalf(65519.0f));
        assertShortEquals(0x7bff, toHalf(65519.9f));
        assertShortEquals(POSITIVE_INFINITY, toHalf(65520.0f));
        assertShortEquals(NEGATIVE_INFINITY, toHalf(-65520.0f));
        // Check if numbers are rounded to nearest even when they
        // cannot be accurately represented by Half
        assertShortEquals(0x6800, toHalf(2049.0f));
        assertShortEquals(0x6c00, toHalf(4098.0f));
        assertShortEquals(0x7000, toHalf(8196.0f));
        assertShortEquals(0x7400, toHalf(16392.0f));
        assertShortEquals(0x7800, toHalf(32784.0f));
    }

    public void testHalfToSingle() {
        // Zeroes, NaN and infinities
        assertEquals(0.0f, toFloat(toHalf(0.0f)), 1e-6f);
        assertEquals(-0.0f, toFloat(toHalf(-0.0f)), 1e-6f);
        assertEquals(Float.NaN, toFloat(toHalf(Float.NaN)), 1e-6f);
        assertEquals(Float.POSITIVE_INFINITY, toFloat(toHalf(Float.POSITIVE_INFINITY)), 1e-6f);
        assertEquals(Float.NEGATIVE_INFINITY, toFloat(toHalf(Float.NEGATIVE_INFINITY)), 1e-6f);
        // Known values
        assertEquals(1.0009765625f, toFloat(toHalf(1.0009765625f)), 1e-6f);
        assertEquals(-2.0f, toFloat(toHalf(-2.0f)), 1e-6f);
        assertEquals(6.1035156e-5f, toFloat(toHalf(6.10352e-5f)), 1e-6f); // Inexact
        assertEquals(65504.0f, toFloat(toHalf(65504.0f)), 1e-6f);
        assertEquals(0.33325195f, toFloat(toHalf(1.0f / 3.0f)), 1e-6f); // Inexact
        // Denormals (flushed to +/-0)
        assertEquals(6.097555e-5f, toFloat(toHalf(6.09756e-5f)), 1e-6f);
        assertEquals(5.9604645e-8f, toFloat(toHalf(5.96046e-8f)), 1e-9f);
        assertEquals(-6.097555e-5f, toFloat(toHalf(-6.09756e-5f)), 1e-6f);
        assertEquals(-5.9604645e-8f, toFloat(toHalf(-5.96046e-8f)), 1e-9f);
    }

    public void testHexString() {
        assertEquals("NaN", toHexString(NaN));
        assertEquals("Infinity", toHexString(POSITIVE_INFINITY));
        assertEquals("-Infinity", toHexString(NEGATIVE_INFINITY));
        assertEquals("0x0.0p0", toHexString(POSITIVE_ZERO));
        assertEquals("-0x0.0p0", toHexString(NEGATIVE_ZERO));
        assertEquals("0x1.0p0", toHexString(toHalf(1.0f)));
        assertEquals("-0x1.0p0", toHexString(toHalf(-1.0f)));
        assertEquals("0x1.0p1", toHexString(toHalf(2.0f)));
        assertEquals("0x1.0p8", toHexString(toHalf(256.0f)));
        assertEquals("0x1.0p-1", toHexString(toHalf(0.5f)));
        assertEquals("0x1.0p-2", toHexString(toHalf(0.25f)));
        assertEquals("0x1.3ffp15", toHexString(MAX_VALUE));
        assertEquals("0x0.1p-14", toHexString(MIN_VALUE));
        assertEquals("0x1.0p-14", toHexString(MIN_NORMAL));
        assertEquals("-0x1.3ffp15", toHexString(LOWEST_VALUE));
    }

    public void testIsInfinite() {
        assertTrue(FP16.isInfinite(POSITIVE_INFINITY));
        assertTrue(FP16.isInfinite(NEGATIVE_INFINITY));
        assertFalse(FP16.isInfinite(POSITIVE_ZERO));
        assertFalse(FP16.isInfinite(NEGATIVE_ZERO));
        assertFalse(FP16.isInfinite(NaN));
        assertFalse(FP16.isInfinite(MAX_VALUE));
        assertFalse(FP16.isInfinite(LOWEST_VALUE));
        assertFalse(FP16.isInfinite(toHalf(-128.3f)));
        assertFalse(FP16.isInfinite(toHalf(128.3f)));
    }

    public void testIsNaN() {
        assertFalse(FP16.isNaN(POSITIVE_INFINITY));
        assertFalse(FP16.isNaN(NEGATIVE_INFINITY));
        assertFalse(FP16.isNaN(POSITIVE_ZERO));
        assertFalse(FP16.isNaN(NEGATIVE_ZERO));
        assertTrue(FP16.isNaN(NaN));
        assertTrue(FP16.isNaN((short) 0x7c01));
        assertTrue(FP16.isNaN((short) 0x7c18));
        assertTrue(FP16.isNaN((short) 0xfc01));
        assertTrue(FP16.isNaN((short) 0xfc98));
        assertFalse(FP16.isNaN(MAX_VALUE));
        assertFalse(FP16.isNaN(LOWEST_VALUE));
        assertFalse(FP16.isNaN(toHalf(-128.3f)));
        assertFalse(FP16.isNaN(toHalf(128.3f)));
    }

    public void testIsNormalized() {
        assertFalse(FP16.isNormalized(POSITIVE_INFINITY));
        assertFalse(FP16.isNormalized(NEGATIVE_INFINITY));
        assertFalse(FP16.isNormalized(POSITIVE_ZERO));
        assertFalse(FP16.isNormalized(NEGATIVE_ZERO));
        assertFalse(FP16.isNormalized(NaN));
        assertTrue(FP16.isNormalized(MAX_VALUE));
        assertTrue(FP16.isNormalized(MIN_NORMAL));
        assertTrue(FP16.isNormalized(LOWEST_VALUE));
        assertTrue(FP16.isNormalized(toHalf(-128.3f)));
        assertTrue(FP16.isNormalized(toHalf(128.3f)));
        assertTrue(FP16.isNormalized(toHalf(0.3456f)));
        assertFalse(FP16.isNormalized(MIN_VALUE));
        assertFalse(FP16.isNormalized((short) 0x3ff));
        assertFalse(FP16.isNormalized((short) 0x200));
        assertFalse(FP16.isNormalized((short) 0x100));
    }

    public void testCeil() {
        assertShortEquals(POSITIVE_INFINITY, FP16.ceil(POSITIVE_INFINITY));
        assertShortEquals(NEGATIVE_INFINITY, FP16.ceil(NEGATIVE_INFINITY));
        assertShortEquals(POSITIVE_ZERO, FP16.ceil(POSITIVE_ZERO));
        assertShortEquals(NEGATIVE_ZERO, FP16.ceil(NEGATIVE_ZERO));
        assertShortEquals(NaN, FP16.ceil(NaN));
        assertShortEquals(LOWEST_VALUE, FP16.ceil(LOWEST_VALUE));
        assertEquals(1.0f, toFloat(FP16.ceil(MIN_NORMAL)), 1e-6f);
        assertEquals(1.0f, toFloat(FP16.ceil((short) 0x3ff)), 1e-6f);
        assertEquals(1.0f, toFloat(FP16.ceil(toHalf(0.2f))), 1e-6f);
        assertShortEquals(NEGATIVE_ZERO, FP16.ceil(toHalf(-0.2f)));
        assertEquals(1.0f, toFloat(FP16.ceil(toHalf(0.7f))), 1e-6f);
        assertShortEquals(NEGATIVE_ZERO, FP16.ceil(toHalf(-0.7f)));
        assertEquals(125.0f, toFloat(FP16.ceil(toHalf(124.7f))), 1e-6f);
        assertEquals(-124.0f, toFloat(FP16.ceil(toHalf(-124.7f))), 1e-6f);
        assertEquals(125.0f, toFloat(FP16.ceil(toHalf(124.2f))), 1e-6f);
        assertEquals(-124.0f, toFloat(FP16.ceil(toHalf(-124.2f))), 1e-6f);
    }

    public void testEquals() {
        assertTrue(FP16.equals(POSITIVE_INFINITY, POSITIVE_INFINITY));
        assertTrue(FP16.equals(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
        assertTrue(FP16.equals(POSITIVE_ZERO, POSITIVE_ZERO));
        assertTrue(FP16.equals(NEGATIVE_ZERO, NEGATIVE_ZERO));
        assertTrue(FP16.equals(POSITIVE_ZERO, NEGATIVE_ZERO));
        assertFalse(FP16.equals(NaN, toHalf(12.4f)));
        assertFalse(FP16.equals(toHalf(12.4f), NaN));
        assertFalse(FP16.equals(NaN, NaN));
        assertTrue(FP16.equals(toHalf(12.4f), toHalf(12.4f)));
        assertTrue(FP16.equals(toHalf(-12.4f), toHalf(-12.4f)));
        assertFalse(FP16.equals(toHalf(12.4f), toHalf(0.7f)));
    }

    public void testFloor() {
        assertShortEquals(POSITIVE_INFINITY, FP16.floor(POSITIVE_INFINITY));
        assertShortEquals(NEGATIVE_INFINITY, FP16.floor(NEGATIVE_INFINITY));
        assertShortEquals(POSITIVE_ZERO, FP16.floor(POSITIVE_ZERO));
        assertShortEquals(NEGATIVE_ZERO, FP16.floor(NEGATIVE_ZERO));
        assertShortEquals(NaN, FP16.floor(NaN));
        assertShortEquals(LOWEST_VALUE, FP16.floor(LOWEST_VALUE));
        assertShortEquals(POSITIVE_ZERO, FP16.floor(MIN_NORMAL));
        assertShortEquals(POSITIVE_ZERO, FP16.floor((short) 0x3ff));
        assertShortEquals(POSITIVE_ZERO, FP16.floor(toHalf(0.2f)));
        assertEquals(-1.0f, toFloat(FP16.floor(toHalf(-0.2f))), 1e-6f);
        assertEquals(-1.0f, toFloat(FP16.floor(toHalf(-0.7f))), 1e-6f);
        assertShortEquals(POSITIVE_ZERO, FP16.floor(toHalf(0.7f)));
        assertEquals(124.0f, toFloat(FP16.floor(toHalf(124.7f))), 1e-6f);
        assertEquals(-125.0f, toFloat(FP16.floor(toHalf(-124.7f))), 1e-6f);
        assertEquals(124.0f, toFloat(FP16.floor(toHalf(124.2f))), 1e-6f);
        assertEquals(-125.0f, toFloat(FP16.floor(toHalf(-124.2f))), 1e-6f);
    }

    public void testRint() {
        assertShortEquals(POSITIVE_INFINITY, FP16.rint(POSITIVE_INFINITY));
        assertShortEquals(NEGATIVE_INFINITY, FP16.rint(NEGATIVE_INFINITY));
        assertShortEquals(POSITIVE_ZERO, FP16.rint(POSITIVE_ZERO));
        assertShortEquals(NEGATIVE_ZERO, FP16.rint(NEGATIVE_ZERO));
        assertShortEquals(NaN, FP16.rint(NaN));
        assertShortEquals(LOWEST_VALUE, FP16.rint(LOWEST_VALUE));
        assertShortEquals(POSITIVE_ZERO, FP16.rint(MIN_VALUE));
        assertShortEquals(POSITIVE_ZERO, FP16.rint((short) 0x200));
        assertShortEquals(POSITIVE_ZERO, FP16.rint((short) 0x3ff));
        assertShortEquals(POSITIVE_ZERO, FP16.rint(toHalf(0.2f)));
        assertShortEquals(NEGATIVE_ZERO, FP16.rint(toHalf(-0.2f)));
        assertEquals(1.0f, toFloat(FP16.rint(toHalf(0.7f))), 1e-6f);
        assertEquals(-1.0f, toFloat(FP16.rint(toHalf(-0.7f))), 1e-6f);
        assertEquals(1.0f, toFloat(FP16.rint(toHalf(0.5f))), 1e-6f);
        assertEquals(-1.0f, toFloat(FP16.rint(toHalf(-0.5f))), 1e-6f);
        assertEquals(125.0f, toFloat(FP16.rint(toHalf(124.7f))), 1e-6f);
        assertEquals(-125.0f, toFloat(FP16.rint(toHalf(-124.7f))), 1e-6f);
        assertEquals(124.0f, toFloat(FP16.rint(toHalf(124.2f))), 1e-6f);
        assertEquals(-124.0f, toFloat(FP16.rint(toHalf(-124.2f))), 1e-6f);
    }

    public void testTrunc() {
        assertShortEquals(POSITIVE_INFINITY, FP16.trunc(POSITIVE_INFINITY));
        assertShortEquals(NEGATIVE_INFINITY, FP16.trunc(NEGATIVE_INFINITY));
        assertShortEquals(POSITIVE_ZERO, FP16.trunc(POSITIVE_ZERO));
        assertShortEquals(NEGATIVE_ZERO, FP16.trunc(NEGATIVE_ZERO));
        assertShortEquals(NaN, FP16.trunc(NaN));
        assertShortEquals(LOWEST_VALUE, FP16.trunc(LOWEST_VALUE));
        assertShortEquals(POSITIVE_ZERO, FP16.trunc(toHalf(0.2f)));
        assertShortEquals(NEGATIVE_ZERO, FP16.trunc(toHalf(-0.2f)));
        assertEquals(0.0f, toFloat(FP16.trunc(toHalf(0.7f))), 1e-6f);
        assertEquals(-0.0f, toFloat(FP16.trunc(toHalf(-0.7f))), 1e-6f);
        assertEquals(124.0f, toFloat(FP16.trunc(toHalf(124.7f))), 1e-6f);
        assertEquals(-124.0f, toFloat(FP16.trunc(toHalf(-124.7f))), 1e-6f);
        assertEquals(124.0f, toFloat(FP16.trunc(toHalf(124.2f))), 1e-6f);
        assertEquals(-124.0f, toFloat(FP16.trunc(toHalf(-124.2f))), 1e-6f);
    }

    public void testLess() {
        assertTrue(FP16.less(NEGATIVE_INFINITY, POSITIVE_INFINITY));
        assertTrue(FP16.less(MAX_VALUE, POSITIVE_INFINITY));
        assertFalse(FP16.less(POSITIVE_INFINITY, MAX_VALUE));
        assertFalse(FP16.less(LOWEST_VALUE, NEGATIVE_INFINITY));
        assertTrue(FP16.less(NEGATIVE_INFINITY, LOWEST_VALUE));
        assertFalse(FP16.less(POSITIVE_ZERO, NEGATIVE_ZERO));
        assertFalse(FP16.less(NEGATIVE_ZERO, POSITIVE_ZERO));
        assertFalse(FP16.less(NaN, toHalf(12.3f)));
        assertFalse(FP16.less(toHalf(12.3f), NaN));
        assertTrue(FP16.less(MIN_VALUE, MIN_NORMAL));
        assertFalse(FP16.less(MIN_NORMAL, MIN_VALUE));
        assertTrue(FP16.less(toHalf(12.3f), toHalf(12.4f)));
        assertFalse(FP16.less(toHalf(12.4f), toHalf(12.3f)));
        assertFalse(FP16.less(toHalf(-12.3f), toHalf(-12.4f)));
        assertTrue(FP16.less(toHalf(-12.4f), toHalf(-12.3f)));
        assertTrue(FP16.less(MIN_VALUE, (short) 0x3ff));
    }

    public void testLessEquals() {
        assertTrue(FP16.less(NEGATIVE_INFINITY, POSITIVE_INFINITY));
        assertTrue(FP16.lessEquals(MAX_VALUE, POSITIVE_INFINITY));
        assertFalse(FP16.lessEquals(POSITIVE_INFINITY, MAX_VALUE));
        assertFalse(FP16.lessEquals(LOWEST_VALUE, NEGATIVE_INFINITY));
        assertTrue(FP16.lessEquals(NEGATIVE_INFINITY, LOWEST_VALUE));
        assertTrue(FP16.lessEquals(POSITIVE_ZERO, NEGATIVE_ZERO));
        assertTrue(FP16.lessEquals(NEGATIVE_ZERO, POSITIVE_ZERO));
        assertFalse(FP16.lessEquals(NaN, toHalf(12.3f)));
        assertFalse(FP16.lessEquals(toHalf(12.3f), NaN));
        assertTrue(FP16.lessEquals(MIN_VALUE, MIN_NORMAL));
        assertFalse(FP16.lessEquals(MIN_NORMAL, MIN_VALUE));
        assertTrue(FP16.lessEquals(toHalf(12.3f), toHalf(12.4f)));
        assertFalse(FP16.lessEquals(toHalf(12.4f), toHalf(12.3f)));
        assertFalse(FP16.lessEquals(toHalf(-12.3f), toHalf(-12.4f)));
        assertTrue(FP16.lessEquals(toHalf(-12.4f), toHalf(-12.3f)));
        assertTrue(FP16.less(MIN_VALUE, (short) 0x3ff));
        assertTrue(FP16.lessEquals(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
        assertTrue(FP16.lessEquals(POSITIVE_INFINITY, POSITIVE_INFINITY));
        assertTrue(FP16.lessEquals(toHalf(12.12356f), toHalf(12.12356f)));
        assertTrue(FP16.lessEquals(toHalf(-12.12356f), toHalf(-12.12356f)));
    }

    public void testGreater() {
        assertTrue(FP16.greater(POSITIVE_INFINITY, NEGATIVE_INFINITY));
        assertTrue(FP16.greater(POSITIVE_INFINITY, MAX_VALUE));
        assertFalse(FP16.greater(MAX_VALUE, POSITIVE_INFINITY));
        assertFalse(FP16.greater(NEGATIVE_INFINITY, LOWEST_VALUE));
        assertTrue(FP16.greater(LOWEST_VALUE, NEGATIVE_INFINITY));
        assertFalse(FP16.greater(NEGATIVE_ZERO, POSITIVE_ZERO));
        assertFalse(FP16.greater(POSITIVE_ZERO, NEGATIVE_ZERO));
        assertFalse(FP16.greater(toHalf(12.3f), NaN));
        assertFalse(FP16.greater(NaN, toHalf(12.3f)));
        assertTrue(FP16.greater(MIN_NORMAL, MIN_VALUE));
        assertFalse(FP16.greater(MIN_VALUE, MIN_NORMAL));
        assertTrue(FP16.greater(toHalf(12.4f), toHalf(12.3f)));
        assertFalse(FP16.greater(toHalf(12.3f), toHalf(12.4f)));
        assertFalse(FP16.greater(toHalf(-12.4f), toHalf(-12.3f)));
        assertTrue(FP16.greater(toHalf(-12.3f), toHalf(-12.4f)));
        assertTrue(FP16.greater((short) 0x3ff, MIN_VALUE));
    }

    public void testGreaterEquals() {
        assertTrue(FP16.greaterEquals(POSITIVE_INFINITY, NEGATIVE_INFINITY));
        assertTrue(FP16.greaterEquals(POSITIVE_INFINITY, MAX_VALUE));
        assertFalse(FP16.greaterEquals(MAX_VALUE, POSITIVE_INFINITY));
        assertFalse(FP16.greaterEquals(NEGATIVE_INFINITY, LOWEST_VALUE));
        assertTrue(FP16.greaterEquals(LOWEST_VALUE, NEGATIVE_INFINITY));
        assertTrue(FP16.greaterEquals(NEGATIVE_ZERO, POSITIVE_ZERO));
        assertTrue(FP16.greaterEquals(POSITIVE_ZERO, NEGATIVE_ZERO));
        assertFalse(FP16.greaterEquals(toHalf(12.3f), NaN));
        assertFalse(FP16.greaterEquals(NaN, toHalf(12.3f)));
        assertTrue(FP16.greaterEquals(MIN_NORMAL, MIN_VALUE));
        assertFalse(FP16.greaterEquals(MIN_VALUE, MIN_NORMAL));
        assertTrue(FP16.greaterEquals(toHalf(12.4f), toHalf(12.3f)));
        assertFalse(FP16.greaterEquals(toHalf(12.3f), toHalf(12.4f)));
        assertFalse(FP16.greaterEquals(toHalf(-12.4f), toHalf(-12.3f)));
        assertTrue(FP16.greaterEquals(toHalf(-12.3f), toHalf(-12.4f)));
        assertTrue(FP16.greater((short) 0x3ff, MIN_VALUE));
        assertTrue(FP16.lessEquals(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
        assertTrue(FP16.lessEquals(POSITIVE_INFINITY, POSITIVE_INFINITY));
        assertTrue(FP16.lessEquals(toHalf(12.12356f), toHalf(12.12356f)));
        assertTrue(FP16.lessEquals(toHalf(-12.12356f), toHalf(-12.12356f)));
    }

    public void testMin() {
        assertShortEquals(NEGATIVE_INFINITY, FP16.min(POSITIVE_INFINITY, NEGATIVE_INFINITY));
        assertShortEquals(NEGATIVE_ZERO, FP16.min(POSITIVE_ZERO, NEGATIVE_ZERO));
        assertShortEquals(NaN, FP16.min(NaN, LOWEST_VALUE));
        assertShortEquals(NaN, FP16.min(LOWEST_VALUE, NaN));
        assertShortEquals(NEGATIVE_INFINITY, FP16.min(NEGATIVE_INFINITY, LOWEST_VALUE));
        assertShortEquals(MAX_VALUE, FP16.min(POSITIVE_INFINITY, MAX_VALUE));
        assertShortEquals(MIN_VALUE, FP16.min(MIN_VALUE, MIN_NORMAL));
        assertShortEquals(POSITIVE_ZERO, FP16.min(MIN_VALUE, POSITIVE_ZERO));
        assertShortEquals(POSITIVE_ZERO, FP16.min(MIN_NORMAL, POSITIVE_ZERO));
        assertShortEquals(toHalf(-3.456f), FP16.min(toHalf(-3.456f), toHalf(-3.453f)));
        assertShortEquals(toHalf(3.453f), FP16.min(toHalf(3.456f), toHalf(3.453f)));
    }

    public void testMax() {
        assertShortEquals(POSITIVE_INFINITY, FP16.max(POSITIVE_INFINITY, NEGATIVE_INFINITY));
        assertShortEquals(POSITIVE_ZERO, FP16.max(POSITIVE_ZERO, NEGATIVE_ZERO));
        assertShortEquals(NaN, FP16.max(NaN, MAX_VALUE));
        assertShortEquals(NaN, FP16.max(MAX_VALUE, NaN));
        assertShortEquals(LOWEST_VALUE, FP16.max(NEGATIVE_INFINITY, LOWEST_VALUE));
        assertShortEquals(POSITIVE_INFINITY, FP16.max(POSITIVE_INFINITY, MAX_VALUE));
        assertShortEquals(MIN_NORMAL, FP16.max(MIN_VALUE, MIN_NORMAL));
        assertShortEquals(MIN_VALUE, FP16.max(MIN_VALUE, POSITIVE_ZERO));
        assertShortEquals(MIN_NORMAL, FP16.max(MIN_NORMAL, POSITIVE_ZERO));
        assertShortEquals(toHalf(-3.453f), FP16.max(toHalf(-3.456f), toHalf(-3.453f)));
        assertShortEquals(toHalf(3.456f), FP16.max(toHalf(3.456f), toHalf(3.453f)));
    }

    public void testCompare() {
        assertEquals(0, FP16.compare(NaN, NaN));
        assertEquals(0, FP16.compare(NaN, (short) 0xfc98));
        assertEquals(1, FP16.compare(NaN, POSITIVE_INFINITY));
        assertEquals(-1, FP16.compare(POSITIVE_INFINITY, NaN));

        assertEquals(0, FP16.compare(POSITIVE_INFINITY, POSITIVE_INFINITY));
        assertEquals(0, FP16.compare(NEGATIVE_INFINITY, NEGATIVE_INFINITY));
        assertEquals(1, FP16.compare(POSITIVE_INFINITY, NEGATIVE_INFINITY));
        assertEquals(-1, FP16.compare(NEGATIVE_INFINITY, POSITIVE_INFINITY));

        assertEquals(0, FP16.compare(POSITIVE_ZERO, POSITIVE_ZERO));
        assertEquals(0, FP16.compare(NEGATIVE_ZERO, NEGATIVE_ZERO));
        assertEquals(1, FP16.compare(POSITIVE_ZERO, NEGATIVE_ZERO));
        assertEquals(-1, FP16.compare(NEGATIVE_ZERO, POSITIVE_ZERO));

        assertEquals(0, FP16.compare(toHalf(12.462f), toHalf(12.462f)));
        assertEquals(0, FP16.compare(toHalf(-12.462f), toHalf(-12.462f)));
        assertEquals(1, FP16.compare(toHalf(12.462f), toHalf(-12.462f)));
        assertEquals(-1, FP16.compare(toHalf(-12.462f), toHalf(12.462f)));
    }
}
