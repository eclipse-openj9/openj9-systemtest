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

package net.openj9.test.arithmetics;

import static org.junit.Assert.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.util.Arrays;

import com.ibm.dataaccess.DecimalData;
import com.ibm.dataaccess.PackedDecimal;
import com.ibm.dataaccess.ExternalDecimal;

import org.junit.Test;
import org.junit.Assert;

public class TestValidityChecking extends TestArithmeticComparisonBase
{
    private byte[] pd = new byte[100];

    // there are several things we need to check:
    // 1. first 4 bits
    // 2. sign
    // 3. digits (1 and several)

    public void testFirst4Bits()
    {
        for (int i = 0; i < 5; ++i)
        {
            fillRandomPD(pd, i, 2);

            Assert.assertEquals(2, PackedDecimal.checkPackedDecimal(pd, i, 2, true, true));

            verifyFirst4Bits(pd, i);

            // add noise to the first 4 bits
            setNoiseAtFirst4Bits(pd, i, 2);
        }
    }

    @Test
    public void testCheckingPerformance()
    {
        // initialization
        fillRandomPD(pd, 0, 15);

        // test execution
        int rv = PackedDecimal.checkPackedDecimal(pd, 0, 15, true, true);

        // verification
        Assert.assertEquals(0, rv);
    }

    @Test
    public void testNormalCases()
    {
        for (int offset = 0; offset < 5; ++offset)
            for (int precision = 1; precision < 10; ++precision)
            {
                Arrays.fill(pd, (byte) 0);
                fillRandomPD(pd, offset, precision);
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, offset, precision, true, true));
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, offset, precision, true, false));
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, offset, precision, false, false));
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, offset, precision, false, true));
            }

        for (int precision = 10; precision < 50; ++precision)
        {
            Arrays.fill(pd, (byte) 0);
            fillRandomPD(pd, 0, precision);
            Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, 0, precision, true, true));
            Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, 0, precision, true, false));
            Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, 0, precision, false, false));
            Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, 0, precision, false, true));
        }
    }

    public void rv1Scenario()
    {
        for (int i = 0; i < 5; ++i)// offset
        {
            for (int precision = 1; precision < 10; ++precision)
            {

            }
        }
    }

    @Test
    public void illInputTest()
    {
        boolean catched = false;
        // ill precision
        try
        {
            PackedDecimal.checkPackedDecimal(null, 0, 0);
        }
        catch (IllegalArgumentException iae)
        {
            catched = true;
        }
        catch (NullPointerException e)
        {
            catched = true;
        }
        finally
        {
            if (catched != true)
            {
                fail("Ill-precision Exception not catched.");
            }
        }

        // ill offset
        try
        {
            catched = false;
            PackedDecimal.checkPackedDecimal(op1, -1, 1);
        }
        catch (IndexOutOfBoundsException e)
        {
            catched = true;
        }
        finally
        {
            if (catched != true)
                fail("Exception not catched.");
        }
    }

    @Test
    public void testOptions()
    {
        // first option
        // 1. when it is turned off, it can still pass when the first few bits
        // are tampered.
        // 2. when it is turned on, and the second operand
        for (int i = 0; i < 5; ++i) // offset
        {
            for (int j = 2; j < 10; ++j) // precision
            {
                Arrays.fill(pd, (byte) 0);
                fillRandomPD(pd, i, j);
                setNoiseAtFirst4Bits(pd, i, j);
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, i, j, false, true));
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, i, j, true, true));
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, i, j, false, false));
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, i, j, true, false));
            }
        }

        // second option
        for (int i = 0; i < 5; ++i)
        {
            for (int j = 1; j < 10; ++j)
            {
                Arrays.fill(pd, (byte) 0);
                fillRandomPD(pd, i, j);
                setNoiseAtFirst4Bits(pd, i, j);
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, i, j, false, true));
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, i, j, true, true));
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, i, j, false, false));
                Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, i, j, true, false));
            }
        }
    }

    @Test
    public void testSign()
    {
        // sign is not correct
        for (int i = 0; i < 5; ++i)
            // offset
            for (int j = 1; j < 10; ++j) // precision
            {
                Arrays.fill(pd, (byte) 0);
                fillRandomPD(pd, i, j);
                addSignNoise(pd, i, j);
                Assert.assertEquals(1, PackedDecimal.checkPackedDecimal(pd, i, j, true, true));
                Assert.assertEquals(1, PackedDecimal.checkPackedDecimal(pd, i, j, true, false));
                Assert.assertEquals(1, PackedDecimal.checkPackedDecimal(pd, i, j, false, true));
                Assert.assertEquals(1, PackedDecimal.checkPackedDecimal(pd, i, j, false, false));
            }

        // sign is not correct and at least one digit is not correct
        for (int i = 0; i < 5; ++i)
            // offset
            for (int j = 2; j < 5; ++j)
                // precision
                for (int k = 1; k < j; ++k) // num of wrong digits
                {
                    Arrays.fill(pd, (byte) 0);
                    getWrongFormatPDHelper2(pd, i, j, k, true, true);
                    Assert.assertEquals(3, PackedDecimal.checkPackedDecimal(pd, i, j, true, true));

                    Arrays.fill(pd, (byte) 0);
                    getWrongFormatPDHelper2(pd, i, j, k, true, true);
                    Assert.assertEquals(3, PackedDecimal.checkPackedDecimal(pd, i, j, true, false));

                    Arrays.fill(pd, (byte) 0);
                    getWrongFormatPDHelper2(pd, i, j, k, true, true);
                    Assert.assertEquals(3, PackedDecimal.checkPackedDecimal(pd, i, j, false, true));

                    Arrays.fill(pd, (byte) 0);
                    getWrongFormatPDHelper2(pd, i, j, k, true, true);
                    Assert.assertEquals(3, PackedDecimal.checkPackedDecimal(pd, i, j, false, false));
                }
    }

    @Test
    public void testDigits()
    {
        // sign is correct, digits are not correct
        for (int i = 0; i < 5; ++i)
            // offset
            for (int j = 1; j < 5; ++j)
                // precision
                for (int k = 1; k < j; ++k) // num of wrong digits
                {
                    Arrays.fill(pd, (byte) 0);
                    getWrongFormatPDHelper2(pd, i, j, k, true, false);
                    Assert.assertEquals(2, PackedDecimal.checkPackedDecimal(pd, i, j, true, false));
                    Assert.assertEquals(2, PackedDecimal.checkPackedDecimal(pd, i, j, true, false));
                    Assert.assertEquals(2, PackedDecimal.checkPackedDecimal(pd, i, j, true, false));
                    Assert.assertEquals(2, PackedDecimal.checkPackedDecimal(pd, i, j, true, false));
                }
    }

    public static void verifyDigits(byte[] pd, int offset, int precision)
    {
        boolean high4Bits = true;
        if (precision % 2 == 0)
        {
            high4Bits = false;
        }

        for (int i = offset; i < offset + precision / 2 + 1;)
        {
            if (high4Bits)
            {
                int comp = pd[i] & 0xF0;
                comp >>= 4;
                Assert.assertTrue(comp >= 0 && comp <= 9);
                high4Bits = !high4Bits;
            } else
            // !high4Bits
            {
                int comp = pd[i] & 0x0F;
                i++;
                Assert.assertTrue(comp >= 0 && comp <= 9);
                high4Bits = !high4Bits;
            }
        }
    }

    public static void verifySign(byte[] pd, int offset, int precision)
    {
        int position = offset + precision / 2;
        int comp = pd[position] & 0x0F;
        Assert.assertTrue(comp >= 10 && comp <= 15);
    }

    public static void verifyFirst4Bits(byte[] pd, int offset)
    {
        // caution: only works when precision % 2 == 0
        int comp = (pd[offset] & 0xF0) >> 4;
        Assert.assertTrue(comp == 0x00);
    }

    @Test
    public void testCheckPackedDecimal4Parameters()
    {
        byte[] pd = new byte[10];
        DecimalData.convertIntegerToPackedDecimal(999, pd, 0, 3, false);
        Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, 0, 3, true));

        DecimalData.convertIntegerToPackedDecimal(999, pd, 0, 3, false);
        Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, 0, 3, false));
    }

    @Test
    public void testCheckPackedDecimal3Parameters()
    {
        byte[] pd = new byte[10];
        DecimalData.convertIntegerToPackedDecimal(999, pd, 0, 3, false);
        Assert.assertEquals(0, PackedDecimal.checkPackedDecimal(pd, 0, 3));

    }

    @Test
    public void testCheckPackedDecimalDifferentSignValues()
    {
        byte[] pd = new byte[10];

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1A;
        int result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1A with overwrite set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1B;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1B with overwrite set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1C;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1C with overwrite set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1D;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1D with overwrite set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1E;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1E with overwrite set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1F;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1F with overwrite set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1A;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1A with overwrite set as true and ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1B;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1B with overwrite set as true and ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1C;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1C with overwrite set as true and ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1D;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1D with overwrite set as true and ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1E;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1E with overwrite set as true and ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1F;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, true);
        Assert.assertEquals("Test CheckPackedDecimal for 1F with overwrite set as true and ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1A;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1A with ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1B;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1B with ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1C;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1C with ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1D;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1D with ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1E;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1E with ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1F;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, true, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1F with ignoreEvenNibble set as true", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1A;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1A", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1B;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1B", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1C;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1C", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1D;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1D", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1E;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1E", 0, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x1F;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 1, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for 1F", 0, result);

    }

    @Test
    public void testInvalidDigits()
    {
        byte[] pd = new byte[10];

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x2A;
        pd[1] = 0x1C;
        int result = PackedDecimal.checkPackedDecimal(pd, 0, 3, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for improper digit in the middle", 2, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x21;
        pd[1] = (byte) 0xAC;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 3, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for improper digit in lowest nibble", 2, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xAA;
        pd[1] = (byte) 0xAC;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 3, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for improper digit in all digits", 2, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xA1;
        pd[1] = (byte) 0x2D;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 3, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for improper digit highest nibble", 2, result);
    }

    @Test
    public void testInvalidSignDigits()
    {
        byte[] pd = new byte[10];

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x21;
        pd[1] = (byte) 0x11;
        int result = PackedDecimal.checkPackedDecimal(pd, 0, 3, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for improper sign digit", 1, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0x99;
        pd[1] = (byte) 0x90;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 3, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for for 0 sign digit", 1, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0x11;
        pd[1] = (byte) 0x29;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 3, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for 9 sign digit", 1, result);

    }

    @Test
    public void testInvalidDigitsAndSignDigit()
    {
        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x21;
        pd[1] = 0x11;
        int result = PackedDecimal.checkPackedDecimal(pd, 0, 4, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for no space for sign digit", 1, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x21;
        pd[1] = 0x11;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 2, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for invalid sign digit and invalid digit outside of precision", 1, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = 0x2C;
        pd[1] = 0x11;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 3, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for invalid sign digit and invalid digit in the middle", 3, result);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xA0;
        pd[1] = 0x00;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 3, false, false);
        Assert.assertEquals("Test CheckPackedDecimal for invalid sign digit and invalid digit in the highest nibble", 3, result);
    }

    @Test
    public void testIgnoreEvenNibble()
    {
        byte[] pd = new byte[10];
        byte[] resultArray = new byte[10];

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xA1;
        pd[1] = (byte) 0x2D;
        resultArray[0] = (byte) 0xA1;
        resultArray[1] = (byte) 0x2D;

        int result = PackedDecimal.checkPackedDecimal(pd, 0, 2, true, false);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set ignore nibble as true", 0, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set ignore nibble as true", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xA1;
        pd[1] = (byte) 0x21;
        resultArray[0] = (byte) 0xA1;
        resultArray[1] = (byte) 0x21;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 2, true, false);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set ignore nibble as true, sign invalid", 1, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set ignore nibble as true, sign invalid", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xAA;
        pd[1] = (byte) 0x2C;
        resultArray[0] = (byte) 0xAA;
        resultArray[1] = (byte) 0x2C;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 2, true, false);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set ignore nibble as true, digits invalid", 2, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set ignore nibble as true, digits invalid", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xAA;
        pd[1] = (byte) 0x21;
        resultArray[0] = (byte) 0xAA;
        resultArray[1] = (byte) 0x21;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 2, true, false);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set ignore nibble as true, digits and sign invalid", 3, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set ignore nibble as true, digits and sign invalid", resultArray, pd);

    }

    @Test
    public void testOverwriteEvenNibble()
    {

        byte[] pd = new byte[10];
        byte[] resultArray = new byte[10];

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xA1;
        pd[1] = (byte) 0x2D;
        resultArray[0] = (byte) 0x01;
        resultArray[1] = (byte) 0x2D;

        int result = PackedDecimal.checkPackedDecimal(pd, 0, 2, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite as true", 0, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite as true", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xA1;
        pd[1] = (byte) 0x21;
        resultArray[0] = (byte) 0x01;
        resultArray[1] = (byte) 0x21;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 2, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite as true, sign invalid", 1, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite as true, sign invalid", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xAA;
        pd[1] = (byte) 0x2C;
        resultArray[0] = (byte) 0x0A;
        resultArray[1] = (byte) 0x2C;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 2, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite as true, digit invalid", 2, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite as true, digit invalid", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xAA;
        pd[1] = (byte) 0x21;
        resultArray[0] = (byte) 0x0A;
        resultArray[1] = (byte) 0x21;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 2, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite as true, digit and sign invalid", 3, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite as true, digit and sign invalid", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0x12;
        pd[1] = (byte) 0x2C;
        resultArray[0] = (byte) 0x12;
        resultArray[1] = (byte) 0x2C;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 3, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite as true, digit and sign invalid", 0, result);
        assertArrayEquals("Test CheckPackedDecimal for everything valid overwrite set as true", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0x12;
        pd[1] = (byte) 0xA1;
        pd[2] = (byte) 0x21;

        resultArray[0] = (byte) 0x02;
        resultArray[1] = (byte) 0xA1;
        resultArray[2] = (byte) 0x21;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 4, false, true);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite as true, digit and sign invalid", 3, result);
        assertArrayEquals("Test CheckPackedDecimal for everything valid overwrite set as true", resultArray, pd);

    }

    @Test
    public void testOverwriteEvenNibbleAndtestIgnoreEvenNibble()
    {

        byte[] pd = new byte[10];
        byte[] resultArray = new byte[10];

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xA1;
        pd[1] = (byte) 0x2D;
        resultArray[0] = (byte) 0x01;
        resultArray[1] = (byte) 0x2D;

        int result = PackedDecimal.checkPackedDecimal(pd, 0, 2, true, true);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite and ignorehighnible as true", 0, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite and ignorehighnible as true", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xA1;
        pd[1] = (byte) 0x21;
        resultArray[0] = (byte) 0x01;
        resultArray[1] = (byte) 0x21;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 2, true, true);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite and ignorehighnible as true, sign invalid", 1, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite and ignorehighnible as true, sign invalid", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xAA;
        pd[1] = (byte) 0x2C;
        resultArray[0] = (byte) 0x0A;
        resultArray[1] = (byte) 0x2C;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 2, true, true);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite and ignorehighnible as true, digit invalid", 2, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite and ignorehighnible as true, digit invalid", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0xAA;
        pd[1] = (byte) 0x21;
        resultArray[0] = (byte) 0x0A;
        resultArray[1] = (byte) 0x21;
        result = PackedDecimal.checkPackedDecimal(pd, 0, 2, true, true);
        Assert.assertEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite and ignorehighnible as true, sign and digit invalid", 3, result);
        assertArrayEquals("Test CheckPackedDecimal for top nibble out of precision, set overwrite and ignorehighnible as true, sign and digit invalid", resultArray, pd);

    }

    @Test
    public void testCheckPackedWithOffset()
    {
        byte[] pd = new byte[10];
        byte[] resultArray = new byte[10];

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0x11;
        pd[1] = 0x12;
        pd[2] = 0x3D;

        resultArray[0] = (byte) 0x11;
        resultArray[1] = 0x02;
        resultArray[2] = 0x3D;

        int result = PackedDecimal.checkPackedDecimal(pd, 1, 2, true, true);
        Assert.assertEquals("Test CheckPackedDecimal with offset, set overwrite and ignorehighnibble as true", 0, result);
        assertArrayEquals("Test CheckPackedDecimal with offset, set overwrite and ignorehighnibble as true", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0x11;
        pd[1] = 0x12;
        pd[2] = 0x3D;

        resultArray[0] = (byte) 0x11;
        resultArray[1] = 0x12;
        resultArray[2] = 0x3D;

        result = PackedDecimal.checkPackedDecimal(pd, 1, 2, true, false);
        Assert.assertEquals("Test CheckPackedDecimal with offset, set ignorehighnibble as true", 0, result);
        assertArrayEquals("Test CheckPackedDecimal with offset, set ignorehighnibble as true", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0x11;
        pd[1] = 0x12;
        pd[2] = 0x3D;

        resultArray[0] = (byte) 0x11;
        resultArray[1] = 0x02;
        resultArray[2] = 0x3D;

        result = PackedDecimal.checkPackedDecimal(pd, 1, 2, false, true);
        Assert.assertEquals("Test CheckPackedDecimal with offset, set overwrite as true", 0, result);
        assertArrayEquals("Test CheckPackedDecimal with offset, set overwrite as true", resultArray, pd);

        Arrays.fill(pd, (byte) 0);
        pd[0] = (byte) 0x11;
        pd[1] = 0x12;
        pd[2] = 0x3D;

        resultArray[0] = (byte) 0x11;
        resultArray[1] = 0x12;
        resultArray[2] = 0x3D;

        result = PackedDecimal.checkPackedDecimal(pd, 1, 2, false, false);
        Assert.assertEquals("Test CheckPackedDecimal with offset", 0, result);
        assertArrayEquals("Test CheckPackedDecimal with offset", resultArray, pd);

    }

    @Test
    public void testEmptyArrayForCheckPacked()
    {

        byte[] pd = new byte[10];

        Arrays.fill(pd, (byte) 0);

        int result = PackedDecimal.checkPackedDecimal(pd, 0, 19, false, false);
        Assert.assertEquals("Test CheckPackedDecimal with empty array", 1, result);

    }

    @Test
    public void testFailingArguments()
    {
        final byte[] a = new byte[] {(byte) 0x40, (byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xC3};
        int result = ExternalDecimal.checkExternalDecimal(a, 0, 5, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 5);
        Assert.assertEquals("Precision == bytesWithSpaces should result in return code of 3", 3, result);

        result = ExternalDecimal.checkExternalDecimal(a, 0, 5, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, -5);
        Assert.assertEquals("bytesWithSpaces < 0 should result in return code of 3", 3, result);

        result = ExternalDecimal.checkExternalDecimal(a, 2, 5, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 1);
        Assert.assertEquals("bytesWithSpaces < 0 should result in return code of 3", 2, result);

        try {
            ExternalDecimal.checkExternalDecimal(a, 0, -5, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 5);
        }
        catch (IllegalArgumentException e) {
            e.getMessage().contains("Precision must be greater than 0");
        }

        try {
            ExternalDecimal.checkExternalDecimal(a, 0, 0, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 5);
        }
        catch (IllegalArgumentException e) {
            e.getMessage().contains("Precision must be greater than 0");
        }

        try {
            ExternalDecimal.checkExternalDecimal(a, -1, 3, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 5);
        }
        catch (IllegalArgumentException e) {
            e.getMessage().contains("Offset must be non-negative integer");
        }

        try {
            ExternalDecimal.checkExternalDecimal(a, 0, 3, 23, 5);
        }
        catch (IllegalArgumentException e) {
            e.getMessage().contains("Invalid decimalType");
        }

        try {
            ExternalDecimal.checkExternalDecimal(a, 0, 9, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 5);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.getMessage().contains("Array access index out of bounds");
        }
    }

    @Test
    public void testExternalDecimalWithSpace()
    {
        final byte[] a = new byte[] {(byte) 0x40, (byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xC3};
        int result = ExternalDecimal.checkExternalDecimal(a, 0, 5, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 1); // rc = 2
        Assert.assertEquals("Result code for signEmbeddedTrailingPrefered(a) should have been 0", 0, result); // hardware returns 2

        // Debugging - Start
        result = ExternalDecimal.checkExternalDecimal(a, 0, 5, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 2); // rc = 0
        // it honors DSC < DC but maybe it needs it to be +1 than actual value?
        // bytes with spaces set to 2 instead of 1 is considered valid external decimal
        Assert.assertEquals("Result code for signEmbeddedTrailingPrefered(a) should have been 0", 0, result); // hardware returns 2

        final byte[] a1 = new byte[] {(byte) 0xF1, (byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xC3};
        result = ExternalDecimal.checkExternalDecimal(a, 0, 5, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 1); // rc = ?
        Assert.assertEquals("Result code for signEmbeddedTrailingPrefered(a) should have been 0", 0, result);

        final byte[] a2 = new byte[] {(byte) 0x40, (byte) 0x40, (byte) 0xF1, (byte) 0xF2, (byte) 0xC3};
        result = ExternalDecimal.checkExternalDecimal(a, 0, 5, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 2); // rc = ?
        Assert.assertEquals("Result code for signEmbeddedTrailingPrefered(a) should have been 0", 0, result);
        // Debugging - End

        final byte[] b = new byte[] {(byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40,
                                     (byte) 0x40, (byte) 0x40, (byte) 0xF9, (byte) 0xF1, (byte) 0xF2,
                                     (byte) 0xC3};
        result = ExternalDecimal.checkExternalDecimal(b, 0, 11, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 7);
        Assert.assertEquals("Result code for signEmbeddedTrailingPrefered(b) should have been 0", 0, result); // hardware returns 2

        result = ExternalDecimal.checkExternalDecimal(b, 0, 11, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 5);
        Assert.assertEquals("Result code for signEmbeddedTrailingPrefered(b) should have been 2", 2, result);

        final byte[] c = new byte[] {(byte) 0x40, (byte) 0xC9, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(c, 0, 5, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 1);
        Assert.assertEquals("Result code for signEmbeddedTrailingPrefered(c) should have been 2", 2, result);

        result = ExternalDecimal.checkExternalDecimal(c, 0, 5, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingPrefered(c) should have been 3", 3, result);

        final byte[] d = new byte[] {(byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40,
                                     (byte) 0x40, (byte) 0x40, (byte) 0xC9, (byte) 0xF1, (byte) 0xF2,
                                     (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(d, 0, 11, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 7);
        Assert.assertEquals("Result code for signEmbeddedTrailingPrefered(d) should have been 3", 3, result);

        result = ExternalDecimal.checkExternalDecimal(d, 0, 11, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 5);
        // hardware returned 2, so need to double check what is expected here
        Assert.assertEquals("Result code for signEmbeddedTrailingPrefered(d) should have been 3", 3, result);

        result = ExternalDecimal.checkExternalDecimal(d, 0, 9, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 10);
        Assert.assertEquals("Result code for signEmbeddedTrailingPrefered(d) should have been 3", 3, result);

        final byte[] e = new byte[] {(byte) 0x40, (byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xF2, (byte) 0xF2, (byte) 0xC3};
        result = ExternalDecimal.checkExternalDecimal(e, 2, 5, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 1);
        Assert.assertEquals("Result code for valid external decimal with offset > bytesWithSpaces", 0, result);
    }

    @Test
    public void testCheckExternalDecimalPreferredPlusSign()
    {
        // Preferred Plus Sign: C
        final byte[] signEmbeddedTrailingPreferedPlus = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xC3};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailingPreferedPlus, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingPreferedPlus should have been 0", 0, result);

        final byte[] signEmbeddedLeadingPreferedPlus = new byte[] {(byte) 0xC9, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeadingPreferedPlus, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeadingPreferedPlus should have been 0", 0, result);

        final byte[] signSeparateTrailingPreferedPlus = new byte[] {(byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0x4e};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailingPreferedPlus, 0, 3, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailingPreferedPlus should have been 0", 0, result);

        final byte[] signSeparateLeadingPreferedPlus = new byte[] {(byte) 0x4e, (byte) 0xF9, (byte) 0xF1, (byte) 0xF2};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeadingPreferedPlus, 0, 3, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeadingPreferedPlus should have been 0", 0, result);
    }

    @Test
    public void testCheckExternalDecimal16Digits()
    {
        // Test max digits a vector register can hold (16)
        final byte[] signEmbeddedTrailing = new byte[] {(byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                        (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF2, (byte) 0xC3};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailing, 0, 16, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailing should have been 0", 0, result);

        final byte[] signEmbeddedLeading = new byte[] {(byte) 0xC6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                       (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeading, 0, 16, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeading should have been 0", 0, result);

        final byte[] signSeparateTrailing = new byte[] {(byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                        (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF2, (byte) 0x4E};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailing, 0, 15, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailing should have been 0", 0, result);

        final byte[] signSeparateLeading = new byte[] {(byte) 0x4E, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                       (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF2, (byte) 0xF4};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeading, 0, 15, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeading should have been 0", 0, result);
    }

    @Test
    public void testCheckExternalDecimal25Digits()
    {
        // Test low and high vector register
        final byte[] signEmbeddedTrailing = new byte[] {(byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                        (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                        (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xC3};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailing, 0, 25, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailing should have been 0", 0, result);

        final byte[] signEmbeddedLeading = new byte[] {(byte) 0xC6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                       (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                       (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeading, 0, 25, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeading should have been 0", 0, result);

        final byte[] signSeparateTrailing = new byte[] {(byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                        (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                        (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0x4E};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailing, 0, 24, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailing should have been 0", 0, result);

        final byte[] signSeparateLeading = new byte[] {(byte) 0x4E, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                       (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                       (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF4};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeading, 0, 24, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeading should have been 0", 0, result);
    }

    @Test
    public void testCheckExternalDecimal32Digits()
    {
        // Test max both vector registers maxed out
        final byte[] signEmbeddedTrailing = new byte[] {(byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF5, (byte) 0xF1, (byte) 0xF8, (byte) 0xF2, (byte) 0xF4,
                                                        (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                        (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                        (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF3, (byte) 0xF4,
                                                        (byte) 0xC3};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailing, 0, 31, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailing should have been 0", 0, result);

        final byte[] signEmbeddedLeading = new byte[] {(byte) 0xC6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF5, (byte) 0xF1, (byte) 0xF8, (byte) 0xF2, (byte) 0xF4,
                                                       (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                       (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                       (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF3, (byte) 0xF4,
                                                       (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeading, 0, 31, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeading should have been 0", 0, result);

        final byte[] signSeparateTrailing = new byte[] {(byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF5, (byte) 0xF1, (byte) 0xF8, (byte) 0xF2, (byte) 0xF4,
                                                        (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                        (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                        (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF3, (byte) 0xF4,
                                                        (byte) 0xF2, (byte) 0x4E};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailing, 0,31, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailing should have been 0", 0, result);

        final byte[] signSeparateLeading = new byte[] {(byte) 0x4E, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF5, (byte) 0xF1, (byte) 0xF8, (byte) 0xF2, (byte) 0xF4,
                                                       (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                       (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                       (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF3, (byte) 0xF4,
                                                       (byte) 0xF2, (byte) 0xF4};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeading, 0, 31, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeading should have been 0", 0, result);
    }

    @Test
    public void testCheckExternalDecimalPreferredMinusSign()
    {
        // Preferred Minus Sign: D
        final byte[] signEmbeddedTrailingPreferedMinus = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xD3};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailingPreferedMinus, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingPreferedMinus should have been 0", 0, result);

        final byte[] signEmbeddedLeadingPreferedMinus = new byte[] {(byte) 0xD9, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeadingPreferedMinus, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeadingPreferedMinus should have been 0", 0, result);

        final byte[] signSeparateTrailingPreferedMinus = new byte[] {(byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0x60};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailingPreferedMinus, 0, 3, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailingPreferedMinus should have been 0", 0, result);

        final byte[] signSeparateLeadingPreferedMinus = new byte[] {(byte) 0x60, (byte) 0xF9, (byte) 0xF1, (byte) 0xF2};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeadingPreferedMinus, 0, 3, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeadingPreferedMinus should have been 0", 0, result);

        // Preferred Minus Sign: B
        final byte[] signEmbeddedTrailingAlternatePlusB = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xB3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailingAlternatePlusB, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingAlternatePlusB should have been 0", 0, result);

        final byte[] signEmbeddedLeadingAlternatePlusB = new byte[] {(byte) 0xB9, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeadingAlternatePlusB, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeadingAlternatePlusB should have been 0", 0, result);
    }

    @Test
    public void testCheckExternalDecimalAlternatePlusSign()
    {
        // Alternate Plus Sign: A
        final byte[] signEmbeddedTrailingAlternatePlusA = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xA3};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailingAlternatePlusA, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingAlternatePlusA should have been 0", 0, result);

        final byte[] signEmbeddedLeadingAlternatePlusA = new byte[] {(byte) 0xA9, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeadingAlternatePlusA, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeadingAlternatePlusA should have been 0", 0, result);

        // Alternate Plus Sign: E
        final byte[] signEmbeddedTrailingAlternatePlusE = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xE3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailingAlternatePlusE, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingAlternatePlusE should have been 0", 0, result);

        final byte[] signEmbeddedLeadingAlternatePlusE = new byte[] {(byte) 0xE9, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeadingAlternatePlusE, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeadingAlternatePlusE should have been 0", 0, result);

        // Alternate Plus Sign: F
        final byte[] signEmbeddedTrailingAlternatePlusF = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailingAlternatePlusF, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingAlternatePlusF should have been 0", 0, result);

        final byte[] signEmbeddedLeadingAlternatePlusF = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeadingAlternatePlusF, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeadingAlternatePlusF should have been 0", 0, result);
    }

    @Test
    public void testCheckExternalDecimalInvalidDigits()
    {
        // Sign embedded trailing
        final byte[] signEmbeddedTrailingInvalidDigitZone = new byte[] {(byte) 0xA9, (byte) 0xF1, (byte) 0xF2, (byte) 0xB3};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailingInvalidDigitZone, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingInvalidDigitZone should have been 2", 2, result);

        final byte[] signEmbeddedTrailingInvalidDigitValue = new byte[] {(byte) 0xF9, (byte) 0xFD, (byte) 0xF2, (byte) 0xB3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailingInvalidDigitValue, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingInvalidDigitValue should have been 2", 2, result);

        // Sign embedded leading
        final byte[] signEmbeddedLeadingInvalidDigitZone = new byte[] {(byte) 0xB9, (byte) 0xA1, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeadingInvalidDigitZone, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeadingInvalidDigitZone should have been 2", 2, result);

        final byte[] signEmbeddedLeadingInvalidDigitValue = new byte[] {(byte) 0xB9, (byte) 0xFD, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeadingInvalidDigitValue, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeadingInvalidDigitValue should have been 2", 2, result);

        // Sign separate trailing
        final byte[] signSeparateTrailingInvalidDigitZone = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xA2, (byte) 0xF3, (byte) 0x4E};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailingInvalidDigitZone, 0, 4, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailingInvalidDigitZone should have been 2", 2, result);

        final byte[] signSeparateTrailingInvalidDigitValue = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xFD, (byte) 0x4E};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailingInvalidDigitValue, 0, 4, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailingInvalidDigitValue should have been 2", 2, result);

        // Sign separate leading
        final byte[] signSeparateLeadingInvalidDigitZone = new byte[] {(byte) 0x4E, (byte) 0xF9, (byte) 0xF1, (byte) 0xA2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeadingInvalidDigitZone, 0, 4, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeadingInvalidDigitZone should have been 2", 2, result);

        final byte[] signSeparateLeadingInvalidDigitValue = new byte[] {(byte) 0x4e, (byte) 0xF9, (byte) 0xF1, (byte) 0xFD, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeadingInvalidDigitValue, 0, 4, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeadingInvalidDigitValue should have been 2", 2, result);
    }

    @Test
    public void testCheckExternalDecimalInvalidSignDigits()
    {
        final byte[] signEmbeddedTrailingInvalidSignDigit = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0x23};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailingInvalidSignDigit, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingInvalidSignDigit should have been 1", 1, result);

        final byte[] signEmbeddedLeadingInvalidSignDigit = new byte[] {(byte) 0x49, (byte) 0xF2, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeadingInvalidSignDigit, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeadingInvalidSignDigit should have been 1", 1, result);

        final byte[] signSeparateTrailingInvalidSignDigit = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xF1, (byte) 0x43};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailingInvalidSignDigit, 0, 4, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailingInvalidSignDigit should have been 1", 1, result);

        final byte[] signSeparateLeadingInvalidSignDigit = new byte[] {(byte) 0x43, (byte) 0xF9, (byte) 0xF1, (byte) 0xF1, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeadingInvalidSignDigit, 0, 4, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeadingInvalidSignDigit should have been 1", 1, result);
    }

    @Test
    public void testCheckExternalDecimalInvalidDigitsAndSignDigit()
    {
        final byte[] signEmbeddedTrailingInvalidDigitAndSignDigit = new byte[] {(byte) 0xF9, (byte) 0xFD, (byte) 0xF2, (byte) 0x23};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailingInvalidDigitAndSignDigit, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingInvalidDigitAndSignDigit should have been 3", 3, result);

        final byte[] signEmbeddedLeadingInvalidDigitAndSignDigit = new byte[] {(byte) 0x49, (byte) 0xFD, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeadingInvalidDigitAndSignDigit, 0, 4, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeadingInvalidDigitAndSignDigit should have been 3", 3, result);

        final byte[] signSeparateTrailingInvalidDigitAndSignDigit = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xFD, (byte) 0x4F};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailingInvalidDigitAndSignDigit, 0, 4, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailingInvalidDigitAndSignDigit should have been 3", 3, result);

        final byte[] signSeparateLeadingInvalidDigitAndSignDigit = new byte[] {(byte) 0x4A, (byte) 0xF9, (byte) 0xF1, (byte) 0xFD, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeadingInvalidDigitAndSignDigit, 0, 4, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeadingInvalidDigitAndSignDigit should have been 3", 3, result);
    }

    @Test
    public void testCheckExternalDecimalWithOffset()
    {
        final byte[] signEmbeddedTrailingInvalidDigitAndSignDigit = new byte[] {(byte) 0xF9, (byte) 0xFD, (byte) 0xF2, (byte) 0x23};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailingInvalidDigitAndSignDigit, 1, 3, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailingInvalidDigitAndSignDigit should have been 3", 3, result);

        final byte[] signEmbeddedLeadingInvalidDigitAndSignDigit = new byte[] {(byte) 0x49, (byte) 0xFD, (byte) 0x42, (byte) 0xFD};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeadingInvalidDigitAndSignDigit, 2, 2, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeadingInvalidDigitAndSignDigit should have been 3", 3, result);

        final byte[] signSeparateTrailingInvalidDigitAndSignDigit = new byte[] {(byte) 0xF9, (byte) 0xF1, (byte) 0xF2, (byte) 0xFD, (byte) 0x4F};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailingInvalidDigitAndSignDigit, 1, 3, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailingInvalidDigitAndSignDigit should have been 3", 3, result);

        final byte[] signSeparateLeadingInvalidDigitAndSignDigit = new byte[] {(byte) 0x4A, (byte) 0xF9, (byte) 0xF1, (byte) 0xFD, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeadingInvalidDigitAndSignDigit, 1, 3, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeadingInvalidDigitAndSignDigit should have been 3", 3, result);
    }

    @Test
    public void testCheckExternalDecimal16DigitsWithOffset()
    {
        // Test low and high vector register
        final byte[] signEmbeddedTrailing = new byte[] {(byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xFD, (byte) 0xF4,
                                                        (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                        (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xC3};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailing, 9, 16, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailing should have been 0", 0, result);

        final byte[] signEmbeddedLeading = new byte[] {(byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF7, (byte) 0xF3, (byte) 0xFB, (byte) 0xC1, (byte) 0xF4,
                                                       (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                       (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeading, 9, 16, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeading should have been 0", 0, result);

        final byte[] signSeparateTrailing = new byte[] {(byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF4, (byte) 0xF2,
                                                        (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                        (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0x4E};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailing, 9, 15, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailing should have been 0", 0, result);

        final byte[] signSeparateLeading = new byte[] {(byte) 0xF4, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xFD, (byte) 0xFA, (byte) 0xF2, (byte) 0x4E, (byte) 0x4E,
                                                       (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                       (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF4};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeading, 9, 15, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeading should have been 0", 0, result);
    }

    @Test
    public void testCheckExternalDecimal32DigitsWithOffset()
    {
        // Test max both vector registers maxed out
        final byte[] signEmbeddedTrailing = new byte[] {(byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF4, (byte) 0xF5, (byte) 0xF6, (byte) 0xF8, (byte) 0xF2,
                                                        (byte) 0xF5, (byte) 0xF1, (byte) 0xF8, (byte) 0xF2, (byte) 0xF4,
                                                        (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                        (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                        (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF3, (byte) 0xF4,
                                                        (byte) 0xC3};
        int result = ExternalDecimal.checkExternalDecimal(signEmbeddedTrailing, 5, 31, DecimalData.EBCDIC_SIGN_EMBEDDED_TRAILING, 0);
        Assert.assertEquals("Result code for signEmbeddedTrailing should have been 0", 0, result);

        final byte[] signEmbeddedLeading = new byte[] {(byte) 0xCF, (byte) 0xF3, (byte) 0xF9, (byte) 0xA1, (byte) 0xC4,
                                                       (byte) 0xF4, (byte) 0xF5, (byte) 0xF6, (byte) 0xF8, (byte) 0xF6,
                                                       (byte) 0xF5, (byte) 0xF1, (byte) 0xF8, (byte) 0xF2, (byte) 0xF4,
                                                       (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                       (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                       (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF3, (byte) 0xF4,
                                                       (byte) 0xF4};
        result = ExternalDecimal.checkExternalDecimal(signEmbeddedLeading, 5, 31, DecimalData.EBCDIC_SIGN_EMBEDDED_LEADING, 0);
        Assert.assertEquals("Result code for signEmbeddedLeading should have been 0", 0, result);

        final byte[] signSeparateTrailing = new byte[] {(byte) 0xFA, (byte) 0xF3, (byte) 0xF9, (byte) 0xFA, (byte) 0xFD,
                                                        (byte) 0xF4, (byte) 0xF5, (byte) 0xF6, (byte) 0xF8, (byte) 0xF2,
                                                        (byte) 0xF5, (byte) 0xF1, (byte) 0xF8, (byte) 0xF2, (byte) 0xF4,
                                                        (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                        (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                        (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                        (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF3, (byte) 0xF4,
                                                        (byte) 0xF2, (byte) 0x4E};
        result = ExternalDecimal.checkExternalDecimal(signSeparateTrailing, 5,31, DecimalData.EBCDIC_SIGN_SEPARATE_TRAILING, 0);
        Assert.assertEquals("Result code for signSeparateTrailing should have been 0", 0, result);

        final byte[] signSeparateLeading = new byte[] {(byte) 0xFA, (byte) 0xF3, (byte) 0xF9, (byte) 0xFA, (byte) 0xFD,
                                                       (byte) 0x4E, (byte) 0xF5, (byte) 0xF6, (byte) 0xF8, (byte) 0xF2,
                                                       (byte) 0xF5, (byte) 0xF1, (byte) 0xF8, (byte) 0xF2, (byte) 0xF4,
                                                       (byte) 0xF7, (byte) 0xF3, (byte) 0xF2, (byte) 0xF1, (byte) 0xF4,
                                                       (byte) 0xF6, (byte) 0xF3, (byte) 0xF9, (byte) 0xF1, (byte) 0xF3,
                                                       (byte) 0xF8, (byte) 0xF1, (byte) 0xF2, (byte) 0xF9, (byte) 0xF1,
                                                       (byte) 0xF3, (byte) 0xF8, (byte) 0xF1, (byte) 0xF3, (byte) 0xF4,
                                                       (byte) 0xF2, (byte) 0xF4};
        result = ExternalDecimal.checkExternalDecimal(signSeparateLeading, 5, 31, DecimalData.EBCDIC_SIGN_SEPARATE_LEADING, 0);
        Assert.assertEquals("Result code for signSeparateLeading should have been 0", 0, result);
    }

    public static void main(String[] args)
    {
        Result result = new Result();
        result = JUnitCore.runClasses(TestValidityChecking.class);
        System.out.println(result.getRunCount());
    }

}
