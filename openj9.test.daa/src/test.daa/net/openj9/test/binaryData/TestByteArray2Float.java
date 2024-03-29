/*******************************************************************************
* Copyright (c) 2017, 2023 IBM Corp.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which accompanies this distribution
* and is available at http://eclipse.org/legal/epl-2.0 or the Apache License, 
* Version 2.0 which accompanies this distribution and is available at 
* https://www.apache.org/licenses/LICENSE-2.0.
* 
* This Source Code may also be made available under the following Secondary
* Licenses when the conditions for such availability set forth in the 
* Eclipse Public License, v. 2.0 are satisfied: GNU General Public License,
* version 2 with the GNU Classpath Exception [1] and GNU General Public License,
* version 2 with the OpenJDK Assembly Exception [2].
* 
* [1] https://www.gnu.org/software/classpath/license.html
* [2] https://openjdk.org/legal/assembly-exception.html
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0-only WITH Classpath-exception-2.0 OR GPL-2.0-only WITH OpenJDK-assembly-exception-1.0
*******************************************************************************/

package net.openj9.test.binaryData;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.dataaccess.ByteArrayUnmarshaller;

public class TestByteArray2Float
{
    static String outputFile;
    static Random randomGen;
    static int ARRAY_SIZE = 256;
    static final int FLOATSIZE = 4;
    static final int BYTESIZE = 8;
    static final int MANTISSA_MASK = 0x007FFFFF;

    /* Attempting to increase the randomness */
    static int randomSeed = (int) (System.currentTimeMillis() % 160001);

    byte[] byteArray = new byte[ARRAY_SIZE];

    static final boolean isBigEndian = true;
    static final boolean isLittleEndian = false;

    static
    {
        outputFile = "expected." + TestShort2ByteArray.class.getSimpleName() + ".txt";
        randomGen = new Random(randomSeed);
    };

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    private float generateRandomFloatWithExponentAndSign(int exponent, boolean negative)
    {
        assert (exponent >= Float.MIN_EXPONENT && exponent <= Float.MAX_EXPONENT);
        exponent += 127;
        exponent <<= 23;
        int rawBits = (randomGen.nextInt() & MANTISSA_MASK) | exponent;
        if (negative)
            return Float.intBitsToFloat(0x80000000 | rawBits);
        else
            return Float.intBitsToFloat(rawBits);
    }

    @Test
    public void testBoundaries()
    {
        byte[] byteArray = new byte[5];

        ByteArrayUnmarshaller.readFloat(byteArray, 0, true);
        ByteArrayUnmarshaller.readFloat(byteArray, 0, false);

        ByteArrayUnmarshaller.readFloat(byteArray, 1, true);
        ByteArrayUnmarshaller.readFloat(byteArray, 1, false);

    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testAIOBLittleEndian1()
    {
        byte[] byteArray = new byte[4];

        ByteArrayUnmarshaller.readFloat(byteArray, 1, false);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testAIOBLittleEndian2()
    {
        byte[] byteArray = new byte[3];

        ByteArrayUnmarshaller.readFloat(byteArray, 0, false);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testAIOBBigEndian1()
    {
        byte[] byteArray = new byte[4];

        ByteArrayUnmarshaller.readFloat(byteArray, 1, true);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testAIOBBigEndian2()
    {
        byte[] byteArray = new byte[3];

        ByteArrayUnmarshaller.readFloat(byteArray, 0, true);
    }

    /* Test for MAX_VALUE */
    @Test
    public void testMaxValue()
    {
        float value = Float.MAX_VALUE;
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);
        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);
        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    /* Test for MAX_VALUE. Random valid offset. */
    @Test
    public void testMaxValueWithRandomValidOffset()
    {
        float value = Float.MAX_VALUE;
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);
        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);
        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    /* Test for MAX_VALUE. Random valid offset. */
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testMaxValueWithInvalidOffset()
    {
        float value = Float.MAX_VALUE;
        int memoryWord = Float.floatToIntBits(value);
        int offset = ARRAY_SIZE - 3;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);
        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);
        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    /* Test for MIN_VALUE */
    @Test
    public void testMinValue()
    {
        float value = Float.MIN_VALUE;
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for MIN_VALUE with a random, valid, offset */
    @Test
    public void testMinValueRandomValidOffset()
    {
        float value = Float.MIN_VALUE;
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for MIN_VALUE with an invalid offset. */
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testMinValueInvalidOffset()
    {
        float value = Float.MIN_VALUE;
        int memoryWord = Float.floatToIntBits(value);
        int offset = ARRAY_SIZE - 300;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for Positive Infinity */
    @Test
    public void testPositiveInfinity()
    {
        float value = Float.POSITIVE_INFINITY;
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for Positive Infinity with random, valid offset */
    @Test
    public void testPositiveInfinityValidOffset()
    {
        float value = Float.POSITIVE_INFINITY;
        int memoryWord = Float.floatToIntBits(value);

        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for Negative Infinity */
    @Test
    public void testNegativeInfinity()
    {
        float value = Float.NEGATIVE_INFINITY;
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for Negative Infinity with a random, valid offset */
    @Test
    public void testNegativeInfinityValidOffset()
    {
        float value = Float.NEGATIVE_INFINITY;
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for NAN */
    @Test
    public void testNAN()
    {
        float value = Float.NaN;
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for NAN */
    @Test
    public void testNANValidOffset()
    {
        float value = Float.NaN;
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for Zero */
    @Test
    public void testZero()
    {
        float value = 0;
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for random positive float with MAX_EXPONENT. */
    @Test
    public void testPositiveFloatMaxExponent()
    {
        float value = generateRandomFloatWithExponentAndSign(Float.MAX_EXPONENT, false);
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    /* Test for random positive float with MAX_EXPONENT with a valid offset */
    @Test
    public void testPositiveFloatMaxExponentValidOffset()
    {
        float value = generateRandomFloatWithExponentAndSign(Float.MAX_EXPONENT, false);
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    /* Test for random negative float with MAX_EXPONENT. */
    @Test
    public void testNegativeFloatMaxExponent()
    {
        float value = generateRandomFloatWithExponentAndSign(Float.MAX_EXPONENT, true);
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    /* Test for random negative float with MAX_EXPONENT with a valid offset */
    @Test
    public void testNegativeFloatMaxExponentValidOffset()
    {
        float value = generateRandomFloatWithExponentAndSign(Float.MAX_EXPONENT, true);
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for random positive float with MIN_EXPONENT */
    @Test
    public void testPositiveFloatMinExponent()
    {
        float value = generateRandomFloatWithExponentAndSign(Float.MIN_EXPONENT, false);
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    /* Test for random positive float with MIN_EXPONENT with a valid offset */
    @Test
    public void testPositiveFloatMinExponentValidOffset()
    {
        float value = generateRandomFloatWithExponentAndSign(Float.MIN_EXPONENT, false);
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    /* Test for random negative float with MIN_EXPONENT */
    @Test
    public void testNegativeFloatMinExponent()
    {
        float value = generateRandomFloatWithExponentAndSign(Float.MIN_EXPONENT, true);
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for random negative float with MIN_EXPONENT with a valid offset */
    @Test
    public void testNegativeFloatMinExponentValidOffset()
    {
        float value = generateRandomFloatWithExponentAndSign(Float.MIN_EXPONENT, true);
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for zero exponent */
    @Test
    public void testZeroExponent()
    {
        float value = generateRandomFloatWithExponentAndSign(0, true);
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Test for zero exponent */
    @Test
    public void testZeroExponentValidOffset()
    {
        float value = generateRandomFloatWithExponentAndSign(0, true);
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /*
     * We have covered all possible boundary cases. We'll now generate floats
     * with a few random exponents and test their reverse conversions. Two
     * positve and two negative exponents.
     */

    @Test
    public void testRandomPositiveFloatPositveExponent()
    {
        int exponent = randomGen.nextInt() % Float.MAX_EXPONENT;
        if (exponent > 0)
            exponent *= -1;
        float value = generateRandomFloatWithExponentAndSign(exponent, false);
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);
        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    @Test
    public void testRandomPositiveFloatPositveExponentValidOffset()
    {
        int exponent = randomGen.nextInt() % Float.MAX_EXPONENT;
        if (exponent > 0)
            exponent *= -1;
        float value = generateRandomFloatWithExponentAndSign(exponent, false);
        int memoryWord = Float.floatToIntBits(value);

        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    @Test
    public void testRandomPositiveFloatNegativeExponent()
    {
        int exponent = randomGen.nextInt() % Float.MAX_EXPONENT;
        if (exponent > 0)
            exponent *= -1;

        float value = generateRandomFloatWithExponentAndSign(exponent, false);
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    @Test
    public void testRandomPositiveFloatNegativeExponentValidOffset()
    {
        int exponent = randomGen.nextInt() % Float.MAX_EXPONENT;
        if (exponent > 0)
            exponent *= -1;

        float value = generateRandomFloatWithExponentAndSign(exponent, false);
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    @Test
    public void testRandomNegativeFloatPositiveExponent()
    {
        int exponent = randomGen.nextInt() % Float.MAX_EXPONENT;
        if (exponent < 0)
            exponent *= -1;

        float value = generateRandomFloatWithExponentAndSign(exponent, true);
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    @Test
    public void testRandomNegativeFloatPositiveExponentValidOffset()
    {
        int exponent = randomGen.nextInt() % Float.MAX_EXPONENT;
        if (exponent < 0)
            exponent *= -1;

        float value = generateRandomFloatWithExponentAndSign(exponent, true);
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    @Test
    public void testRandomNegativeFloatNegativeExponent()
    {
        int exponent = randomGen.nextInt() % Float.MAX_EXPONENT;
        if (exponent > 0)
            exponent *= -1;
        float value = generateRandomFloatWithExponentAndSign(exponent, true);
        int memoryWord = Float.floatToIntBits(value);
        int offset = 0;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    @Test
    public void testRandomNegativeFloatNegativeExponentValidOffset()
    {
        int exponent = randomGen.nextInt() % Float.MAX_EXPONENT;
        if (exponent > 0)
            exponent *= -1;
        float value = generateRandomFloatWithExponentAndSign(exponent, true);
        int memoryWord = Float.floatToIntBits(value);
        int offset = randomGen.nextInt() % (ARRAY_SIZE - 4);
        offset = offset > 0 ? offset : -offset;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);
    }

    /* Testing for the maximum valid offset */
    @Test
    public void testMaximumValidOffset()
    {
        int exponent = randomGen.nextInt() % Float.MAX_EXPONENT;
        if (exponent < 0)
            exponent *= -1;
        float value = generateRandomFloatWithExponentAndSign(exponent, true);
        int memoryWord = Float.floatToIntBits(value);
        int offset = ARRAY_SIZE - 4;

        /* Big Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * (FLOATSIZE - 1 - i)) & 0x0ff);

        float resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        assertEquals(value, resultFloat, 0.0);

        /* Little Endian */
        Arrays.fill(byteArray, (byte) 0x0);

        for (int i = 0; i < FLOATSIZE; i++)
            byteArray[offset + i] = (byte) ((memoryWord >> BYTESIZE * i) & 0x0ff);

        resultFloat = ByteArrayUnmarshaller.readFloat(byteArray, offset, isLittleEndian);
        assertEquals(value, resultFloat, 0.0);

    }

    /* Test for null byte array */
    @Test(expected = NullPointerException.class)
    public void testNullByteArray()
    {
        int offset = 0;
        byte[] nullByteArray = null;

        ByteArrayUnmarshaller.readFloat(nullByteArray, offset, isBigEndian);
        ByteArrayUnmarshaller.readFloat(nullByteArray, offset, isLittleEndian);
    }

    @Test
    public void testIncrementalOnebyte()
    {
        int offset = 0;
        for (int i = 0; i < ARRAY_SIZE; i++)
        {
            byteArray[i] = (byte) (float) (i > ARRAY_SIZE / 2 ? -(i - ARRAY_SIZE / 2) : i);
        }

        for (int i = 0; i < ARRAY_SIZE; i++)
        {
            ByteArrayUnmarshaller.readFloat(byteArray, offset, isBigEndian);
        }
    }

}
