package com.res.java.lib;

/*****************************************************************************
Copyright 2009 Venkat Krishnamurthy
This file is part of RES.

RES is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

RES is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with RES.  If not, see <http://www.gnu.org/licenses/>.

@author VenkatK mailto: open.cobol.to.java at gmail.com
 ******************************************************************************/
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import com.res.common.RESConfig;
import com.res.java.lib.exceptions.InvalidCobolFormatException;
import com.res.java.lib.exceptions.OverflowException;

/**
 * Data representation in COBOL memory format.
 * Has support for DISPLAY,PACKED-DECIMAL,BINARY usages
 * Numbers in Java types can be int,long,BigDecimal.
 * Has also support for String and byte[] Java types which
 * represent quoted strings and data groups respectively in traslated COBOL.
 *
 * The underlying data-structure is a byte[] buffer into which data(numbers,String etc.)
 * are stored in COBOL memory format and read back into Java types.
 *
 * To compensate for the performance of this back and forth conversion,
 * options -javatype and -javatype2 used while translating to represent data
 * directly in Java types and convert into COBOL format only when necessary.
 * This class is used to represent data always in cases of -nojavtype option OR
 * using REDEFINEs and in some other cases including FILLERs.
 */
public class CobolBytes {

    public static final int BINARY = Constants.BINARY;
    public static final int DISPLAY = Constants.DISPLAY;
    public static final int PACKED_DECIMAL = Constants.PACKED_DECIMAL;

    public static class Field {

        String name = null, picture = null;
        FieldFormat fmt_ = null;

        public FieldFormat formatter() {
            if (fmt_ == null) {
                fmt_ = new FieldFormat(name, picture);
            }
            return fmt_;
        }
    }

    public static class StringField extends Field {

        public StringField(String n, String p) {
            name = n;
            picture = p;
        }

        public String get(CobolBytes thiz, int o, int len) {
            return thiz.toString(o, len);
        }

        public void set(CobolBytes thiz, int o, int len, String f) {
            thiz.valueOf(o, len, f);
        }
    }

    public static class CharField extends Field {

        public CharField(String n, String p) {
            name = n;
            picture = p;
        }

        public char get(CobolBytes thiz, int o, int len) {
            return thiz.getChar(o);
        }

        public void set(CobolBytes thiz, int o, int len, char f) {
            thiz.setChar(o, f);
        }
    }

    public static class ByteField extends Field {

        public ByteField(String n, String p) {
            name = n;
            picture = p;
        }

        public byte get(CobolBytes thiz, int o, int len) {
            return thiz.getByte(o);
        }

        public void set(CobolBytes thiz, int o, int len, byte f) {
            thiz.setByte(o, f);
        }
    }

    public static class BytesField extends Field {

        public BytesField(String n, String p) {
            name = n;
            picture = p;
        }

        public byte[] get(CobolBytes thiz, int o, int len) {
            return thiz.getBytes(o, len);
        }

        public void set(CobolBytes thiz, int o, int len, byte[] f) {
            thiz.valueOf(o, len, f, 0);
        }
    }

    public static class IntField extends Field {

        int usage;
        boolean isSigned, isSignLeading, isSignSeperate;

        public IntField(String n, String p, int u, boolean iss) {
            name = n;
            picture = p;
            usage = u;
            isSigned = iss;
            isSignLeading = isSignSeperate = false;
        }

        public IntField(String n, String p, int u, boolean iss, boolean isl, boolean issep) {
            name = n;
            picture = p;
            usage = u;
            isSigned = iss;
            isSignLeading = isl;
            isSignSeperate = issep;
        }

        public int get(CobolBytes thiz, int o, int l) {
            switch (usage) {
                case BINARY:
                    return thiz.getBinaryInt(o, l);
                case DISPLAY:
                    return thiz.getDisplayInt(o, l, isSigned, isSignLeading, isSignSeperate);
                case PACKED_DECIMAL:
                    return thiz.getPackedDecimalInt(o, l);
            }
            return 0;
        }

        public void set(CobolBytes thiz, int o, int l, int f) {
            switch (usage) {
                case BINARY:
                    thiz.setBinaryInt(o, l, f, isSigned);
                    break;
                case DISPLAY:
                    thiz.setDisplayInt(o, l, f, isSigned, isSignLeading, isSignSeperate);
                    break;
                case PACKED_DECIMAL:
                    thiz.setPackedDecimalInt(o, l, f, isSigned);
                    break;
            }
        }
    }

    public static class LongField extends Field {

        int usage;
        boolean isSigned, isSignLeading, isSignSeperate;

        public LongField(String n, String p, int u, boolean iss) {
            usage = u;
            isSigned = iss;
            isSignLeading = isSignSeperate = false;
        }

        public LongField(String n, String p, int u, boolean iss, boolean isl, boolean issep) {
            usage = u;
            picture = p;
            name = n;
            isSigned = iss;
            isSignLeading = isl;
            isSignSeperate = issep;
        }

        public long get(CobolBytes thiz, int o, int l) {
            switch (usage) {
                case BINARY:
                    return thiz.getBinaryLong(o, l);
                case DISPLAY:
                    return thiz.getDisplayLong(o, l, isSigned, isSignLeading, isSignSeperate);
                case PACKED_DECIMAL:
                    return thiz.getPackedDecimalLong(o, l);
            }
            return 0;
        }

        public void set(CobolBytes thiz, int o, int l, long f) {
            switch (usage) {
                case BINARY:
                    thiz.setBinaryLong(o, l, f, isSigned);
                    break;
                case DISPLAY:
                    thiz.setDisplayLong(o, l, f, isSigned, isSignLeading, isSignSeperate);
                    break;
                case PACKED_DECIMAL:
                    thiz.setPackedDecimalLong(o, l, f, isSigned);
                    break;
            }
        }
    }

    public static class BigDecimalField extends Field {

        int usage, scale;
        boolean isSigned, isSignLeading, isSignSeperate;

        public BigDecimalField(String n, String p, int u, int sc, boolean iss) {
            usage = u;
            scale = sc;
            isSigned = iss;
            isSignLeading = isSignSeperate = false;
        }

        public BigDecimalField(String n, String p, int u, int sc, boolean iss, boolean isl, boolean issep) {
            usage = u;
            scale = sc;
            isSigned = iss;
            isSignLeading = isl;
            isSignSeperate = issep;
        }

        public BigDecimal get(CobolBytes thiz, int o, int l) {
            switch (usage) {
                case BINARY:
                    return thiz.getBinaryBigDecimal(o, l, scale);
                case DISPLAY:
                    return thiz.getDisplayBigDecimal(o, l, scale, isSigned, isSignLeading, isSignSeperate);
                case PACKED_DECIMAL:
                    return thiz.getPackedDecimalBigDecimal(o, l, scale);
            }
            return BigDecimal.ZERO;
        }

        public void set(CobolBytes thiz, int o, int l, BigDecimal f) {
            switch (usage) {
                case BINARY:
                    thiz.setBinaryBigDecimal(o, l, f, isSigned);
                    break;
                case DISPLAY:
                    thiz.setDisplayBigDecimal(o, l, f, isSigned, isSignLeading, isSignSeperate);
                    break;
                case PACKED_DECIMAL:
                    thiz.setPackedDecimalBigDecimal(o, l, f, isSigned);
                    break;
            }
        }
    }
    private byte[] bytes = null;
    private ByteBuffer byteBuffer;

    public CobolBytes(int len) {
        init(new byte[len]);
        clear(0, len, (byte) 0);
    }

    public CobolBytes(int len, boolean sp) {
        init(new byte[len]);
        if (sp) {
            clear(0, len, (byte) ' ');
        } else {
            clear(0, len, (byte) 0);
        }
    }

    public CobolBytes(byte[] b) {
        init(b);
    }

    public CobolBytes(CobolBytes b) {
        init(b.get());
    }

    public CobolBytes(byte[] b, int len) {
        if (b.length == len) {
            init(b);
        } else {
            init(new byte[len]);
            valueOf(0, len, b, 0);
        }
    }
    private int globalOffset = 0;

    public CobolBytes(CobolBytes b, int offset, int len) {
        init(b.get());
        globalOffset = offset;
    }

    private void init(byte[] b) {
        bytes = b;
        byteBuffer = ByteBuffer.wrap(bytes);
        byteLongBuffer.order(ByteOrder.LITTLE_ENDIAN);
        if (!RunConfig.getInstance().isBigEndian()) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
        }
    }

    protected CobolBytes() {
    }

    public int size() {
        if (bytes != null) {
            return bytes.length;
        }
        return 0;
    }
    private static byte[] byteLong = new byte[8];
    private static ByteBuffer byteLongBuffer = ByteBuffer.wrap(byteLong);

    public int getInt(int offset, int len) {
        return getBinaryInt(offset, len);
    }

    public void setInt(int offset, int len, int data) {
        setBinaryInt(offset, len, data, true);
    }

    /**
     * Function: COBOL BINARY usage Number from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java int
     */
    public int getBinaryInt(int offset, int len) {
        validate(offset, len = normalizeLength(len));
        if (len <= 0) {
            return 0;
        }
        int fsiz = 4;
        for (int i = fsiz; i < 8; ++i) {
            byteLong[i] = 0;//clear temporary
        }
        byteBuffer.position(offset);
        byteBuffer.get(byteLong, fsiz, len);
        for (int i = 0; i < fsiz; ++i) {
            byteLong[i] = ((byteLong[fsiz] < 0) ? (byte) -1 : (byte) 0);
        }
        return byteLongBuffer.getInt(fsiz);
    }

    /**
     * Function: COBOL BINARY usage Number to buffer/memory
     * Input: Specified offset/address , length, Data(int) and IsSigned flag
     */
    public void setBinaryInt(int offset, int len, int data, boolean isSigned) {
        data = (int) normalizeBinary(data, len, isSigned);
        len = normalizeLength(len);
        validate(offset = normalizeOffset(offset), len);
        int fsiz = 4;
        for (int i = fsiz; i < 8; ++i) {
            byteLong[i] = 0;//clear temporary
        }
        byteLongBuffer.putInt(fsiz, data);
        byteBuffer.position(offset);
        byteBuffer.put(byteLong, fsiz, len);
        return;
    }

    public long getLong(int offset, int len) {
        return getBinaryLong(offset, len);
    }

    public void setLong(int offset, int len, long data) {
        setBinaryLong(offset, len, data, true);
    }

    /**
     * Function: COBOL BINARY usage Number from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java long
     */
    public long getBinaryLong(int offset, int len) {
        if ((len = normalizeLength(len)) <= 0) {
            return 0;
        }
        validate(offset = normalizeOffset(offset), len);
        int fsiz = 0;
        for (int i = 7; i >= 0; --i) {
            byteLong[i] = 0;//clear temporary
        }
        byteBuffer.position(offset);
        byteBuffer.get(byteLong, fsiz, len);
        for (int i = len; i < 8; ++i) {
            byteLong[i] = ((byteLong[len - 1] < 0) ? (byte) -1 : (byte) 0);
        }
        return byteLongBuffer.getLong(0);
    }

    /**
     * Function: COBOL BINARY usage Number to buffer/memory
     * Input: Specified offset/address , length, Data(long) and IsSigned flag
     */
    public void setBinaryLong(int offset, int len, long data, boolean isSigned) {
        data = normalizeBinary(data, len, isSigned);
        len = normalizeLength(len);
        validate(offset = normalizeOffset(offset), len);
        for (int i = 7; i >= 0; --i) {
            byteLong[i] = 0;
        }
        byteLongBuffer.putLong(0, data);
        byteBuffer.position(offset);
        byteBuffer.put(byteLong, 0, len);
        return;
    }

    /**
     * Function: Adjust number of digits in length TO number of bytes in memory need
     * Input: Number of digits
     * Return: Number of bytes in COBOL memory needed.
     */
    private int normalizeLength(int len) {
        if (len >= 1 && len <= 4) {
            len = 2;
        } else if (len >= 5 && len <= 9) {
            len = 4;
        } else if (len >= 10 && len <= 19) {
            len = 8;
        } else {
            len = 0;
        }
        return len;
    }
    private static final long[] normBinary = {1, 10, 100, 1000, 10000, 100000, 1000000,
        10000000, 100000000, 1000000000L, 100000000000L, 1000000000000L,
        10000000000000L, 100000000000000L, 1000000000000000L, 10000000000000000L,
        100000000000000000L, 1000000000000000000L, 1000000000000000000L, 1000000000000000000L
    };

    /**
     * Function: Round a input number to number of digits and take off sign if needed.
     * Input: Number, number of digits, and IsSigned flag.
     * Return: Rounded number.
     */
    public long normalizeBinary(long val, int len, boolean isSigned) {
        if (len <= 0 || len > 21) {
            return 0;
        }
        if (!isSigned) {
            val = Math.abs(val);
        }
        long ret = (len >= 18) ? val : (val % normBinary[len]);
        if (val != ret) {
            __assertArithmeticException();
            //throw new ArithmeticException("Size Error occurred. com.res.java.lib.CobolBytes.");
        }
        return ret;
    }

    private long normalizeDisplay(long val, int len, boolean isSigned) {
        return normalizeBinary(val, len, isSigned);
    }
/*
    public BigDecimal normalizeDisplay(BigDecimal val, int len, int scale, boolean isSigned) {
        return normalizeDecimal(val, len, scale, isSigned);
    }
*/
    public BigDecimal normalizeDisplay(String val, int len, int scale, boolean isSigned) {
        return normalizeDecimal(new BigDecimal(RunTimeUtil.getInstance().stripLeadingZeros(val)).scaleByPowerOfTen(-scale), len, scale, isSigned);
    }

    public long normalizeDisplay(String val, int len, boolean isSigned) {
        return normalizeBinary(new BigDecimal(
                RunTimeUtil.getInstance().stripLeadingZeros(val)).longValue(), len,  isSigned);
    }

    /**
     * Function: COBOL BINARY usage Number from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java BigDecimal
     */
    public BigDecimal getBinaryBigDecimal(int offset, int len, int scale) {
        long l1 = getLong(offset, len);
        return new BigDecimal(BigInteger.valueOf(l1), scale);
    }

    /**
     * Function: COBOL BINARY usage Decimal to buffer/memory
     * Input: Specified offset/address , length, data(BigDecimal) and IsSigned flag
     */
    public void setBinaryBigDecimal(int offset, int len, BigDecimal data, boolean isSigned) {
        setBinaryLong(offset, len, normalizeBinary(data.unscaledValue().longValue(), len, isSigned), isSigned);
    }

    /**
     * Function: COBOL BINARY usage Decimal with rounding to Scale to buffer/memory
     * Input: Specified offset/address , length, data(BigDecimal), scale and IsSigned flag
     */
    public void setBinaryBigDecimal(int offset, int len, BigDecimal data, int scale, boolean isSigned) {
        long dataL = normalizeDecimalLocal(data, len, scale, isSigned);
        len = normalizeLength(len);
        validate(offset = normalizeOffset(offset), len);
        for (int i = 7; i >= 0; --i) {
            byteLong[i] = 0;
        }
        byteLongBuffer.putLong(0, dataL);
        byteBuffer.position(offset);
        byteBuffer.put(byteLong, 0, len);
        return;
    }

    private long normalizeDecimalLocal(BigDecimal val, int len, int scale, boolean isSigned) {
        return normalizeDecimal(val, len, scale, isSigned).unscaledValue().longValue();
    }

    public BigDecimal normalizeDecimal(BigDecimal val, int len, int scale, boolean isSigned) {
        if (len <= 0 || len > 21) {
            return BigDecimal.ZERO;
        }
        val = val.setScale(scale, RoundingMode.DOWN);

        if (!isSigned) {
            val = val.abs();
        }
        BigDecimal iVal = val.setScale(0, RoundingMode.DOWN).setScale(scale, RoundingMode.DOWN);
        BigDecimal ret = (len > 18) ? iVal.remainder(BigDecimal.TEN.pow(len - scale))
                : (iVal.remainder(BigDecimal.valueOf(normBinary[len - scale])));
        if (iVal.compareTo(ret) != 0) {
            __assertArithmeticException();
            //throw new ArithmeticException("Size Error occurred. com.res.java.lib.CobolBytes.");
        }
        return ret.add(val.subtract(iVal)).setScale(scale, RoundingMode.DOWN);
    }

    /**
     * Function: COBOL PACKED-DECIMAL usage Number from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java int
     */
    public int getPackedDecimalInt(int offset, int len) {
        validate(offset, (len = normalizeComp3Length(len)));
        if (len < 1 || len > 4) {
            return 0;
        }
        int fsiz = 8 - len;
        for (int i = 7; i >= 0; --i) {
            byteLong[i] = 0;//clear temporary
        }
        byteBuffer.position(offset);
        byteBuffer.get(byteLong, fsiz, len);
        int ret = 0;
        int aByte;
        int digit;
        for (int i = fsiz; i < 7; ++i) {
            aByte = byteLong[i] & 0xFF;
            ret = ret * 100 + aByte;
        }
        aByte = byteLong[7] & 0xFF;
        digit = aByte >> 4;
        ret = ret * 10 + digit;
        int sign = aByte & 0x0F;
        if (sign == 0x0D) {
            ret = -ret;
        } else {
            if (sign != 0x0C && sign != 0x0F && sign != 0) {
                reportError();
            }
        }
        return ret;
    }

    /**
     * Function: COBOL PACKED-DECIMAL usage number to buffer/memory
     * Input: Specified offset/address , length, Data(int) and IsSigned flag
     */
    public void setPackedDecimalInt(int offset, int len, int data, boolean isSigned) {
        validate(offset, (len = normalizeComp3Length(len)));
        clear(offset, len, (byte) 0x00);
        if (len < 1 || len > 8) {
            return;
        }
        int fsiz = 8 - len;
        for (int i = 7; i >= 0; --i) {
            byteLong[i] = 0;//clear temporary
        }
        if (isSigned) {
            if (data < 0) {
                byteLong[7] = 0x0D;
            } else {
                byteLong[7] = 0x0C;
            }
        } else {
            byteLong[7] = 0x0F;
        }
        data = (int) abs(data);
        int aByte = data % 10;
        byteLong[7] = (byte) (0xFF & (byteLong[7] | (aByte << 4)));
        int i = 6;
        data = data / 10;
        for (; data > 0 && i >= fsiz; data /= 100, i--) {
            byteLong[i] = PACKED_BYTES[data % 100];
        }
        ;
        if (data > 0) {
            __assertOverflow();
            //reportError(new OverflowException("Packed Decimal Integer Overflow on set."));
        }
        byteBuffer.position(offset);
        byteBuffer.put(byteLong, fsiz, len);
    }

    /**
     * Function: COBOL PACKED-DECIMAL usage Number from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java long
     */
    public long getPackedDecimalLong(int offset, int len) {
        validate(offset, (len = normalizeComp3Length(len)));
        if (len < 1 || len > 10) {
            return 0;
        }
        int fsiz = 10 - len;
        byteBuffer.position(offset);
        byteBuffer.get(temp, fsiz, len);
        long ret = 0;
        int aByte;
        int digit;
        for (int i = fsiz; i < 9; ++i) {
            ret = ret * 100 + (temp[i] & 0xFF);
        }
        aByte = temp[9] & 0xFF; // Get next 2 digits & drop sign bits
        digit = aByte >> 4;    // HO first
        ret = ret * 10 + digit;
        int sign = aByte & 0x0F;  // now get sign
        if (sign == 0x0D) {
            ret = -ret;
        } else {
            if (sign != 0x0C && sign != 0x0F && sign != 0) {
                __assertArithmeticException();
            }
        }
        return ret;
    }

    /**
     * Function: Adjust from number digits to number of bytes in memort for
     *     a PACKED-DECIMAL number
     * Input:    Number of digits
     * Return:   Number of bytes in memory
     */
    private int normalizeComp3Length(int len) {
        return (len / 2) + len % 2;
    }

    /**
     * Function: COBOL PACKED-DECIMAL usage number to buffer/memory
     * Input: Specified offset/address , length, Data(long) and IsSigned flag
     */
    public void setPackedDecimalLong(int offset, int len, long data, boolean isSigned) {
        validate(offset, (len = normalizeComp3Length(len)));
        clear(offset, len, (byte) 0x00);
        if (len < 1 || len > 10) {
            return;
        }
        int fsiz = 10 - len;
        if (isSigned) {
            if (data < 0) {
                temp[9] = 0x0D;
            } else {
                temp[9] = 0x0C;
            }
        } else {
            temp[9] = 0x0F;
        }
        data = (long) abs(data);
        int aByte = (int) data % 10;
        temp[9] = (byte) (0xFF & (temp[9] | (aByte << 4)));
        int i = 8;
        data = data / 10;
        for (; data > 0 && i >= fsiz; data /= 100, i--) {
            temp[i] = PACKED_BYTES[(int) data % 100];
        }
        ;
        if (data > 0) {
            __assertArithmeticException();
        }
        byteBuffer.position(offset);
        byteBuffer.put(temp, fsiz, len);
    }

    /**
     * Function: COBOL PACKED-DECIMAL usage decimal from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java BigDecimal
     */
    public BigDecimal getPackedDecimalBigDecimal(int offset, int len, int scale) {
        return new BigDecimal(BigInteger.valueOf(getPackedDecimalLong(offset, len)), scale);
    }

    /**
     * Function: COBOL PACKED-DECIMAL usage decimal number to buffer/memory
     * Input: Specified offset/address , length, Data(BigDecimal) and IsSigned flag
     */
    public void setPackedDecimalBigDecimal(int offset, int len, BigDecimal data, boolean isSigned) {
        setPackedDecimalLong(offset, len, data.unscaledValue().longValue(), isSigned);
    }

    /**
     * Function: COBOL PACKED-DECIMAL usage decimal number with rounding to a scale to buffer/memory
     * Input: Specified offset/address , length, Data(BigDecimal), scale and IsSigned flag
     */
    public void setPackedDecimalBigDecimal(int offset, int len, BigDecimal data, int scale, boolean isSigned) {
        setPackedDecimalLong(offset, len, normalizeDecimalLocal(data, len, scale, isSigned), isSigned);
    }
    private static byte[] temp = new byte[21];

    /**
     * Function: COBOL DISPLAY usage number from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java int
     */
    public int getDisplayInt(int offset, int len, boolean isSigned, boolean isSignLeading, boolean isSignSeperate) {
        validate(offset = normalizeOffset(offset), len);
        if (len < 1 || len > 19) {
            return 0;
        }
        int fsiz = 19 - len;
        byteBuffer.position(offset);
        byteBuffer.get(temp, fsiz, len);
        char sign = '+';
        int firstDigit = fsiz, lastDigit = fsiz + len - 1;
        int ret = 0;
        int anotherByte = 0;
        if (isSigned) {
            if (isSignSeperate) {
                if (isSignLeading) {
                    sign = toAsciiFromEbcdic(bytes[offset]);
                    firstDigit++;
                } else {
                    sign = toAsciiFromEbcdic(bytes[offset + len - 1]);
                    lastDigit--;
                }
                if (sign != '+' && sign != '-') {
                    __assertArithmeticException();
                }
            } else {
                int aByte;
                if (isSignLeading) {
                    aByte = bytes[offset] & 0XF0;
                    ret = bytes[offset] & 0x0F;
                    firstDigit++;
                } else {
                    aByte = 0xF0 & bytes[offset + len - 1];
                    anotherByte = bytes[offset + len - 1] & 0x0F;
                    lastDigit--;
                }
                if (aByte == 0xD0) {
                    sign = '-';
                } else if (aByte != 0XC0 && aByte != 0xF0 && aByte != 0x00) {
                    __assertArithmeticException();
                } else {
                    sign = '+';
                }
            }
        }
        //Not worrying about ASCII or EBCDIC or multinational character set.
        //Assuming that all numbers are adjacent in number starting from '0'.
        for (int i = firstDigit; i <= lastDigit; ++i) {
            if (temp[i] <= '9' && temp[i] >= '0') {
                ret = ret * 10 + (temp[i] - '0');
            } else {
                ret *= 10;
            }
        }

        if (isSigned && !isSignSeperate && !isSignLeading) {
            ret = ret * 10 + anotherByte;
        }
        if (sign == '-') {
            ret = -(ret);
        }
        return ret;
    }

    /**
     * Function: COBOL DISPLAY usage number to buffer/memory
     * Input: Specified offset/address , length, data(int) and IsSigned,
     *     IsSignLeading and IsSignSeparate flags
     */
    public void setDisplayInt(int offset, int len, int data, boolean isSigned, boolean isSignLeading, boolean isSignSeperate) {
        validate(offset = normalizeOffset(offset), len);
        int sign = (data < 0) ? -1 : 1;
        int fsiz = 19 - len;
        for (int i = fsiz; i < 19; ++i) {
            temp[i] = '0';
        }
        data = (int) normalizeDisplay(data, len, isSigned);
        int firstDigit = fsiz, lastDigit = fsiz + len - 1;
        int anotherByte = 0;
        if (isSigned) {
            if (isSignSeperate) {
                if (isSignLeading) {
                    temp[firstDigit] = (byte) ((sign > 0) ? '+' : '-');
                    firstDigit++;
                } else {
                    temp[lastDigit] = (byte) ((sign > 0) ? '+' : '-');
                    lastDigit--;
                }
            } else {
                if (isSignLeading) {
                    anotherByte = ((sign > 0) ? 0xC0 : 0xD0);
                } else {
                    temp[lastDigit--] = (byte) (((byte) (data % 10)) | (byte) ((sign > 0) ? 0xC0 : 0xD0));
                    data = data / 10;
                }
            }
        }

        //Not worrying about ASCII or EBCDIC or multinational character set.
        //Assuming that the low nibble contains the binary value of the zone.
        //Correct me if I am wrong.
        int i = lastDigit;

        while (data > 0 && i >= firstDigit) {
            byte aByte = (byte) ('0' + (data % 10));
            temp[i] = aByte;//the or takes care of sign
            data = data / 10;
            i--;
        }
        ;
        if (isSigned && !isSignSeperate && isSignLeading) {
            temp[firstDigit] = (byte) ((temp[firstDigit] + '0') | (byte) anotherByte);
        }
        byteBuffer.position(offset);
        toEbcdicFromAscii(temp, fsiz, len);
        byteBuffer.put(temp, fsiz, len);
    }

    /**
     * Function: COBOL DISPLAY usage number from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java long
     */
    public long getDisplayLong(int offset, int len, boolean isSigned, boolean isSignLeading, boolean isSignSeperate) {
        validate(offset = normalizeOffset(offset), len);
        if (len < 1 || len > 21) {
            return 0;
        }
        //int fsiz=21-len;
        byteBuffer.position(offset);
        //byteBuffer.get(temp, fsiz, len);
        char sign = '+';
        int firstDigit = offset, lastDigit = offset + len - 1;
        long ret = 0;
        int anotherByte = 0;
        if (isSigned) {
            if (isSignSeperate) {
                if (isSignLeading) {
                    sign = toAsciiFromEbcdic(bytes[offset]);
                    firstDigit++;
                } else {
                    sign = toAsciiFromEbcdic(bytes[offset + len - 1]);
                    lastDigit--;
                }
                if (sign != '+' && sign != '-') {
                    __assertArithmeticException();
                }
            } else {
                int aByte;
                if (isSignLeading) {
                    aByte = bytes[offset] & 0XF0;
                    ret = bytes[offset] & 0x0F;
                    firstDigit++;
                } else {
                    aByte = 0xF0 & bytes[offset + len - 1];
                    anotherByte = bytes[offset + len - 1] & 0x0F;
                    lastDigit--;
                }
                if (aByte == 0xD0) {
                    sign = '-';
                } else if (aByte != 0XC0 && aByte != 0xF0 && aByte != 0x00) {
                    __assertArithmeticException();
                } else {
                    sign = '+';
                }
            }
        }
        //Not worrying about ASCII or EBCDIC or multinational character set.
        //Assuming that all numbers are adjacent in number starting from '0'.
        //toAsciiFromEbcdic(temp,firstDigit,lastDigit-firstDigit+1);
        if (RunConfig.getInstance().isEbcdicMachine()) {
            for (int i = firstDigit; i <= lastDigit; ++i) {
                char c = toAsciiFromEbcdic(bytes[i]);
                if (c <= '9' && c >= '0') {
                    ret = ret * 10 + (temp[i] - '0');
                } else if (c == '.') {
                    lastDigit++;//Scary. But may work.
                } else {
                    ret *= 10;
                }
            }
        } else {
            for (int i = firstDigit; i <= lastDigit; ++i) {
                if (bytes[i] <= '9' && bytes[i] >= '0') {
                    ret = ret * 10 + (bytes[i] - '0');
                } else if (bytes[i] == '.') {
                    lastDigit++;//Scary. But may work.
                }     //else
                //ret*=10;
            }
        }
        if (isSigned && !isSignSeperate && !isSignLeading) {
            ret = ret * 10 + anotherByte;
        }
        if (sign == '-') {
            ret = -(ret);
        }
        return ret;
    }

    /**
     * Function: COBOL DISPLAY usage number to buffer/memory
     * Input: Specified offset/address , length, data(long) and IsSigned,
     *     IsSignLeading and IsSignSeparate flags
     */
    public void setDisplayLong(int offset, int len, long data, boolean isSigned, boolean isSignLeading, boolean isSignSeperate) {
        validate(offset = normalizeOffset(offset), len);
        int sign = (data < 0) ? -1 : 1;
        int fsiz = 21 - len;
        for (int i = 0; i < 21; ++i) {
            temp[i] = '0';
        }
        data = normalizeDisplay(data, len, isSigned);
        int firstDigit = fsiz, lastDigit = fsiz + len - 1;
        int anotherByte = 0;
        if (isSigned) {
            if (isSignSeperate) {
                if (isSignLeading) {
                    temp[firstDigit] = (byte) ((sign > 0) ? '+' : '-');
                    firstDigit++;
                } else {
                    temp[lastDigit] = (byte) ((sign > 0) ? '+' : '-');
                    lastDigit--;
                }
            } else {
                if (isSignLeading) {
                    anotherByte = ((sign > 0) ? 0xC0 : 0xD0);
                } else {
                    temp[lastDigit--] = (byte) (((byte) (data % 10)) | (byte) ((sign > 0) ? 0xC0 : 0xD0));
                    data = data / 10;
                }
            }
        }

        //Not worrying about ASCII or EBCDIC or multinational character set.
        //Assuming that the low nibble contains the binary value of the zone.
        //Correct me if I am wrong.
        int i = lastDigit;

        while (data > 0 && i >= firstDigit) {
            byte aByte = (byte) ('0' + (data % 10));
            temp[i] = aByte;//the or takes care of sign
            data = data / 10;
            i--;
        }
        ;
        toEbcdicFromAscii(temp, firstDigit, lastDigit - firstDigit + 1);
        if (isSigned && !isSignSeperate && isSignLeading) {
            temp[firstDigit] = (byte) (temp[firstDigit] | (byte) anotherByte);
        }
        byteBuffer.position(offset);
        byteBuffer.put(temp, fsiz, len);
    }

    /**
     * Function: COBOL DISPLAY usage decimal number from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java BigDecimal
     */
    public BigDecimal getDisplayBigDecimal(int offset, int len, int scale, boolean isSigned, boolean isSignLeading, boolean isSignSeperate) {
        validate(offset = normalizeOffset(offset), len);
        return new BigDecimal(BigInteger.valueOf(getDisplayLong(offset, len, isSigned, isSignLeading, isSignSeperate)), scale);
    }

    /**
     * Function: COBOL DISPLAY usage decimal number to buffer/memory
     * Input: Specified offset/address , length, data(BigDecimal) and IsSigned,
     *     IsSignLeading and IsSignSeparate flags
     */
    public void setDisplayBigDecimal(int offset, int len, BigDecimal data, boolean isSigned, boolean isSignLeading, boolean isSignSeperate) {
        validate(offset = normalizeOffset(offset), len);
        clear(offset, len, (byte) '0');
        setDisplayLong(offset, len, data.unscaledValue().longValue(), isSigned, isSignLeading, isSignSeperate);
    }

    /**
     * Function: COBOL DISPLAY usage decimal number with rounding to a scale to buffer/memory
     * Input: Specified offset/address , length, data(BigDecimal) , scale and IsSigned,
     *     IsSignLeading and IsSignSeparate flags
     */
    public void setDisplayBigDecimal(int offset, int len, BigDecimal data, int scale, boolean isSigned, boolean isSignLeading, boolean isSignSeperate) {
        validate(offset = normalizeOffset(offset), len);
        clear(offset, len, (byte) '0');
        setDisplayLong(offset, len, normalizeDecimalLocal(data, len, scale, isSigned), isSigned, isSignLeading, isSignSeperate);
    }

    /**
     * Function: COBOL DISPLAY usage quoted string from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java String
     */
    public String getDisplayString(int offset, int len) {
        validate(offset = normalizeOffset(offset), len);
        String s = toString(offset, len);
        return s;
    }

    public char getChar(int offset) {
        return (char) getByte(offset);
    }

    public byte getByte(int offset) {
        validate(offset = normalizeOffset(offset), 1);
        return byteBuffer.get(offset);
    }

    public void setChar(int offset, char data) {
        setByte(offset, (byte) data);
    }

    public void setByte(int offset, byte data) {
        validate(offset = normalizeOffset(offset), 1);
        byteBuffer.put(offset, data);
    }

    /**
     * Function: COBOL DISPLAY quoted string to buffer/memory with padding SPACES.
     * Input: Specified offset/address , length, data(String)
     */
    public void valueOf(int offset, int len, String data) {
        valueOf(offset, len, data.getBytes(), 0);
        /*
        validate(offset=normalizeOffset(offset),len);
        data=normalizeString(data, len);
        System.arraycopy(data.getBytes(), 0, bytes, offset, Math.min(len,data.length()));
         */
    }

    public void valueOf(int offset, int len, String data, boolean rightJust) {
        int dataOffset = 0;
        if (rightJust && data.length() > len) {
            dataOffset = data.length() - len;
        }
        valueOf(offset, len, data.getBytes(), dataOffset);
        /*
        validate(offset=normalizeOffset(offset),len);
        data=normalizeString(data, len);
        System.arraycopy(data.getBytes(), 0, bytes, offset, Math.min(len,data.length()));
         */
    }

    public void valueOf(int offset, int len, byte[] data, boolean rightJust) {
        int dataOffset = 0;
        //if(rightJust&&data.length>len)
        //dataOffset = data.length-len;
        valueOf(offset, len, data, dataOffset);
        /*
        validate(offset=normalizeOffset(offset),len);
        data=normalizeString(data, len);
        System.arraycopy(data.getBytes(), 0, bytes, offset, Math.min(len,data.length()));
         */
    }

    /**
     * Function: adjust COBOL DISPLAY quoted string to a fixed length with padding if necessary
     * Input: data(String) and length
     * Return: Adjusted String
     */
    public String normalizeString(String val, int len, boolean justRight) {
        return normalizeString(val, len, justRight, false, false);
    }

    public String normalizeString(String val, int len, boolean justRight,boolean noDot,boolean addSign) {
        if (val == null)   val = " ";
        if(noDot) {
            val=val.replace(".","");
            val=val.replace('+',' ');
        } else
        if(addSign)val=val.replace('+',' ');

        if (val.length() == len) {
            return val;
        }
        
        char filler = val.charAt(0);
        if(val.length()!=1||(filler!='\"'&&filler!=MIN_VALUE()&&filler!=MAX_VALUE()&&filler!='0')) 
            filler=' ';
 
        if (val.length() < len) {
            StringBuilder ret = new StringBuilder();
            if (!justRight) {
                ret.append(val);
                for (int i = val.length(); i < len; ++i) {
                    ret.append(filler);
                }
            } else {
                for (int i = len - val.length(); i > 0; --i) {
                    ret.append(filler);
                }
                ret.append(val);
            }
            return ret.toString();
        } else {
            __assertOverflow();
            if (!justRight) {
                return val.substring(0, len);
            } else {
                return val.substring(val.length() - len, val.length());
            }
        }
    }

    public String normalizeString(String val, int len) {
        return normalizeString(val, len, false,false,false);
    }

    public byte[] normalizeBytes(byte[] val, int cnt, boolean b) {
        return normalizeString(new String(val), cnt, b,false,false).getBytes();
    }

    /**
     * Function: COBOL DISPLAY usage quoted string from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java String
     */
    public String toString(int offset, int len) {
        return new String(getBytes(offset, len));
    }

    /**
     * Function: COBOL group in a byte[] array to buffer/memory with padding SPACES.
     * Input: Specified offset/address , length, data(byte[]), offset into data array.
     */
    public void valueOf(int offset, int len, byte[] data, int dataOffset) {
        validate(offset = normalizeOffset(offset), len);
        if (data == null) {
            return;
        }
        int l = Math.min(data.length - dataOffset, len);
        System.arraycopy(data, dataOffset, bytes, offset, l);
        clear(offset + l, len - l, data);
    }


    private void clear(int offset, int len, byte[] data) {
        if (data.length == 1) {
            if (data[0] == '\"') {
                clear(offset, len, (byte) '\"');
            } else if (data[0] == '0') {
                clear(offset, len, (byte) '0');
            } else if (data[0] == -1) {
                clear(offset, len, (byte) 0xFF);
            } else if (data[0] == 0x00) {
                clear(offset, len, (byte) 0x00);
            } else {
                clear(offset, len, (byte) ' ');
            }
        } else {
            clear(offset, len, (byte) ' ');
        }
    }

    public void valueOf(int offset, int len, byte[] data) {
        valueOf(offset, len, data, 0);
    }

    /**
     * Function: COBOL group byte[] array from buffer/memory
     * Input: Specified offset/address and length
     * Return: Java byte[] array.
     */
    public byte[] getBytes(int offset, int len) {
        validate(offset = normalizeOffset(offset), len);
        byte[] b = new byte[len];
        System.arraycopy(bytes, offset, b, 0, Math.min(bytes.length - offset, len));
        return b;
    }

    /**
     * Function: retrun the underlying byte[] array representing COBOL memory
     * Return: Java byte[] array.
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Function: retrun the underlying byte[] array representing COBOL memory
     * Return: Java byte[] array.
     */
    public byte[] get() {
        return bytes;
    }

    /**
     * Function: Is the underlying data Numeric?
     * Input: offset and length
     * Return: Java boolean.
     */
    public boolean isNumeric(int offset, int len) {
        return toString(offset, len).matches("[0-9+-]*");
    }

    /**
     * Function: Is the underlying data Alphabetic?
     * Input: offset and length
     * Return: Java boolean.
     */
    public boolean isAlphabetic(int offset, int len) {
        return toString(offset, len).matches("[a-zA-Z ]*");
    }

    /**
     * Function: Is the underlying data Alphabetic Lower?
     * Input: offset and length
     * Return: Java boolean.
     */
    public boolean isAlphabeticLower(int offset, int len) {
        return toString(offset, len).matches("[a-z ]*");
    }

    /**
     * Function: Is the underlying data AlphaNumeric?
     * Input: offset and length
     * Return: Java boolean.
     */
    public boolean isAlphaNumeric(int offset, int len) {
        return toString(offset, len).matches("[a-zA-Z0-9+- ]*");
    }

    /**
     * Function: Is the underlying data Packed Decimal number?
     * Input: offset and length
     * Return: Java boolean.
     */
    public boolean isPackedDecimalNumeric(int offset, int len) {
        validate(offset = normalizeOffset(offset), len);
        if (len < 1 || len > 10) {
            return false;
        }
        byte[] temp = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int fsiz = 10 - len;
        byteBuffer.position(offset);
        byteBuffer.get(temp, fsiz, len);
        int aByte;
        int digit;
        for (int i = fsiz; i < 9; ++i) {
            aByte = bytes[i] & 0xFF; // Get next 2 digits
            digit = aByte >> 4;    // HO first
            if (digit < 0 || digit > 9) {
                return false;
            }
            digit = aByte & 0x0F;      // now LO
            if (digit < 0 || digit > 9) {
                return false;
            }
        }
        aByte = temp[9] & 0xFF; // Get next 2 digits & drop sign bits
        digit = aByte >> 4;    // HO first
        if (digit < 0 || digit > 9) {
            return false;
        }
        int sign = aByte & 0x0F;  // now get sign
        if (sign != 0x0D && sign != 0x0C && sign != 0x0F && sign == 0) {
            return false;
        }
        return true;
    }

    /**
     * Function: clear the underlying buffer with pad
     * Input: offset, length and padding
     */
    private void clear(int offset, int len, byte c) {
        for (int i = 0; i < len; ++i) {
            bytes[offset + i] = c;
        }
    }

    /**
     * Function: adjust Offset
     * Input: offset
     * Return: offset
     */
    private int normalizeOffset(int off) {
        return off + globalOffset;

    }

    /**
     * Function: validate Offset and length
     * Input: offset, length
     * Return: throw Exception
     */
    public void validate(int offset, int len) {
        if (len <= 0) {
            reportError();
        }
        if (bytes == null || offset < 0 || offset + len > bytes.length) {
            reportError("Invalid indexes :@(" + String.valueOf(offset) + ":" + String.valueOf(len) + ")");
        }
        byteBuffer.limit(bytes.length);
    }

    /**
     * Function: Report Error and Exit.
     */
    private static void reportError() {
        try {
            throw new InvalidCobolFormatException("");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Function: Report Error and Exit.
     * Input: message
     */
    private static void reportError(String msg) {
        try {
            System.err.println(msg);
            throw new InvalidCobolFormatException("");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Function: Report Error and Exit.
     * Input: Exception
     */
    private static void reportError(Exception e) {
        try {
            throw e;
        } catch (Exception e2) {
            e2.printStackTrace();
            System.exit(-1);
        }
    }
    private static boolean isEbcdicMachine = false;
    private static boolean isBigEndian = true;
    private static final CharsetDecoder decoder = Charset.forName("IBM1047").newDecoder();
    private static final CharsetEncoder encoder = Charset.forName("IBM1047").newEncoder();
    private static final CharsetDecoder decoder2 = Charset.forName("ASCII").newDecoder();
    @SuppressWarnings("unused")
    private static final CharsetEncoder encoder2 = Charset.forName("ASCII").newEncoder();

    public void toAsciiFromEbcdic(byte[] b, int offset, int len) {
        if (RunConfig.getInstance().isEbcdicMachine()) {
            try {
                decoder.decode(ByteBuffer.wrap(b, offset, len));
            } catch (CharacterCodingException e) {
                reportError(e);
            }
        }
    }

    public static String toAsciiFromEbcdic(String asciiStr) {
        return toAsciiFromEbcdic(asciiStr, RESConfig.getInstance().isEbcdicMachine());
    }

    public static String toAsciiFromEbcdic(String asciiStr, boolean doForce) {
        if (doForce) {
            try {
                byte[] b = asciiStr.getBytes();
                ByteBuffer bb = encoder.encode(decoder2.decode(ByteBuffer.wrap(b)));
                return new String(bb.array());
            } catch (CharacterCodingException e) {
                reportError(e);
            }
        }
        return asciiStr;
    }

    public static char toAsciiFromEbcdic(byte asciiStr) {
        if (RunConfig.getInstance().isEbcdicMachine()) {
            return toAsciiFromEbcdic(String.valueOf(asciiStr)).charAt(0);
        }
        return (char) asciiStr;
    }

    public static boolean isEbcdicMachine() {
        return isEbcdicMachine;
    }

    public void setEbcdicMachine(boolean isEbcdicMachine) {
        CobolBytes.isEbcdicMachine = isEbcdicMachine;
    }

    public void toEbcdicFromAscii(byte[] b, int offset, int len) {
        if (RunConfig.getInstance().isEbcdicMachine()) {
            try {
                //byte[] bt=new byte[len];
                //System.arraycopy(b, offset, bt, 0, len);
                decoder.decode(ByteBuffer.wrap(b, offset, len));
            } catch (CharacterCodingException e) {
                reportError(e);
            }
        }
    }

    public static char toEbcdicFromAscii(byte asciiStr) {
        if (RunConfig.getInstance().isEbcdicMachine()) {
            return toEbcdicFromAscii(String.valueOf(asciiStr)).charAt(0);
        }
        return (char) asciiStr;
    }

    public static String toEbcdicFromAscii(String asciiStr) {
        return toEbcdicFromAscii(asciiStr, RESConfig.getInstance().isEbcdicMachine());
    }

    public static String toEbcdicFromAscii(String asciiStr, boolean force) {
        if (force) {
            try {
                return encoder.encode(CharBuffer.wrap(asciiStr)).asCharBuffer().toString();
            } catch (CharacterCodingException e) {
                reportError(e);
            }
        }
        return asciiStr;
    }

    private int abs(int data) {
        if (data < 0) {
            return 0 - (data);
        }
        return data;
    }

    private long abs(long data) {
        //if(data<0) return 0-(data);
        return Math.abs(data);
    }

    public static void setBigEndian(boolean isBigEndian) {
        CobolBytes.isBigEndian = isBigEndian;
    }

    public static boolean isBigEndian() {
        return isBigEndian;
    }
    private static final byte[] PACKED_BYTES = new byte[]{
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
        30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
        40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
        50, 51, 52, 53, 54, 55, 56, 57, 58, 59,
        60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
        70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
        80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
        90, 91, 92, 93, 94, 95, 96, 97, 98, 99
    };
    private static byte[] long_value_buf = new byte[16];
   // private static ByteBuffer LONG_VALUE_BUF = ByteBuffer.wrap(long_value_buf);

    public static char MAX_VALUE() {
        return (char) 0xFF;
    }

    public static char MIN_VALUE() {
        return (char) 0x00;
    }
/*
    public static String asString(int val) {
        LONG_VALUE_BUF.putInt(0, val);
        return new String(long_value_buf, 0, 4);
    }

    public static String asString(long val) {
        LONG_VALUE_BUF.putLong(0, val);
        return new String(long_value_buf, 0, 8);
    }

    public static String asString(float val) {
        LONG_VALUE_BUF.putFloat(0, val);
        return new String(long_value_buf, 0, 8);
    }

    public static String asString(double val) {
        LONG_VALUE_BUF.putDouble(0, val);
        return new String(long_value_buf, 0, 16);
    }
 * 
 */
    protected boolean isOverflowExceptionOccurred = false;
    protected boolean isArithmeticExceptionOccurred = false;

    protected char __getChar(String string) {
        if (string.length() != 1) {
            __assertOverflow();
        }
        return string.charAt(0);
    }

    protected char __getChar(char string) {
        return string;
    }

    protected char __getChar(byte[] string) {
        if (string.length != 1) {
            __assertOverflow();
        }
        return (char) string[0];
    }

    protected byte __getByte(byte[] string) {
        if (string.length != 1) {
            __assertOverflow();
        }
        return string[0];
    }

    protected byte __getByte(String string) {
        if (string.length() != 1) {
            __assertOverflow();
        }
        return (byte) string.charAt(0);
    }

    protected char __getChar(String string, boolean justRight) {
        if (string.length() != 1) {
            __assertOverflow();
        }
        if (!justRight) {
            return string.charAt(0);
        } else {
            return string.charAt(string.length() - 1);
        }
    }

    protected byte __getByte(byte[] string, boolean justRight) {
        if (string.length != 1) {
            __assertOverflow();
        }
        if (!justRight) {
            return string[0];
        } else {
            return string[string.length - 1];
        }
    }

    private void __assertOverflow() {
        isOverflowExceptionOccurred = true;
        if (__program != null && __program.isExceptionsEnabled) {
            throw new OverflowException("Overflow Error Occurred in accessor.");
        }
    }

    private void __assertArithmeticException() {
        isArithmeticExceptionOccurred = true;
        if (__program != null && __program.isExceptionsEnabled) {
            throw new ArithmeticException("Size Error Occurred in accessor.");
        }
    }
    protected Program __program = null;
/*
    public String toStringWithoutSign(int offset, int len, boolean isSigned, boolean isSignLeading,
            boolean isSignSeparate) {
        StringBuffer raw = new StringBuffer(toString(offset, len));
        if (isSigned) {
            if (isSignSeparate) {
                if (isSignLeading) {
                    raw.delete(0, 1);
                } else {
                    raw.delete(len - 1, len);
                }
            } else {
                if (isSignLeading) {
                    raw.setCharAt(0, (char) (raw.charAt(0) - ((raw.charAt(0) & 0xf0) - '0')));
                } else {
                    raw.setCharAt(len - 1, (char) (raw.charAt(len - 1) - ((raw.charAt(len - 1) & 0xf0) - '0')));
                }

            }
        }
        return raw.toString();
   }
*/
    private String normalizeNumberString(String val, int len) {
       if (val == null)   val = "0";
       
        //val=val.replace(".","");
        
        val=val.replace('+',' ');

        if (val.length() == len) {
            return val;
        }

        char filler = '0';

        if (val.length() < len) {
            StringBuilder ret = new StringBuilder();
            for (int i = len - val.length(); i > 0; --i) {
                ret.append(filler);
            }
            ret.append(val);
            return ret.toString();
        } else {
            __assertOverflow();
            return val.substring(val.length() - len, val.length());
        }
    }

  
    public String toDisplayString(long val, int len, int scale, boolean isSigned, boolean isSignLeading, boolean isSignSeparate) {
        return toDisplayString(new BigDecimal(val).setScale(scale, RoundingMode.DOWN).unscaledValue().longValue(), len, isSigned, isSignLeading, isSignSeparate);
    }

    public String toDisplayString(BigDecimal val, int len, int scale, boolean isSigned, boolean isSignLeading, boolean isSignSeparate) {
        return toDisplayString(val.setScale(scale, RoundingMode.DOWN).unscaledValue().longValue(), len, isSigned, isSignLeading, isSignSeparate);
    }

    public String asDottedSignedString(String val, int len,  boolean isSigned, boolean isSignLeading, boolean isSignSeparate) {
        if(isSigned)len++;
        return normalizeString(val, len,true,false,isSigned);
    }

    public String asDottedSignedString(String val, int len, int scale, int addlScale,boolean isSigned, boolean isSignLeading, boolean isSignSeparate) {
        scale-=addlScale;
        if(isSigned)len++;
        return normalizeString(new BigDecimal(val).scaleByPowerOfTen(-scale).toPlainString(), len-scale,true,false,isSigned);
    }
    public String asDottedSignedString(String val, int len, int scale, boolean isSigned, boolean isSignLeading, boolean isSignSeparate) {
        if(isSigned)len++;
        return normalizeString(new BigDecimal(val).scaleByPowerOfTen(-scale).toPlainString(), len,true,false,isSigned);
    }
    
    //This is a cut-down version of getDisplayLong
    public String toDottedSignedString(int offset, int len, boolean isSigned, boolean isSignLeading, boolean isSignSeparate) {
        return String.valueOf(getDisplayLong(offset, len, isSigned, true, true));
    }

    //This is a cut-down version of getDisplayBigDecimal
    public String toDottedSignedString(int offset, int len, int scale, boolean isSigned, boolean isSignLeading, boolean isSignSeparate) {
        return getDisplayBigDecimal(offset, len, scale, isSigned, true, true).toPlainString();
    }

    public String toDisplayString(long data, int len, boolean isSigned, boolean isSignLeading, boolean isSignSeparate) {   
        if(isSigned)len++;
        int sign = (data < 0) ? -1 : 1;
        data=Math.abs(data);
        int fsiz = 21 - len;
        for (int i = 0; i < 21; ++i) {
            temp[i] = '0';
        }
        data = normalizeDisplay(data, len, isSigned);
        int firstDigit = fsiz, lastDigit = fsiz + len - 1;
        int anotherByte = 0;
        if (isSigned) {
            if (true) {
                if (true) {
                    temp[firstDigit] = (byte) ((sign > 0) ? '+' : '-');
                    firstDigit++;
                } else {
                    temp[lastDigit] = (byte) ((sign > 0) ? '+' : '-');
                    lastDigit--;
                }
            } else {
                if (isSignLeading) {
                    anotherByte = ((sign > 0) ? 0xC0 : 0xD0);
                } else {
                    temp[lastDigit--] = (byte) (((byte) (data % 10)) | (byte) ((sign > 0) ? 0xC0 : 0xD0));
                    data = data / 10;
                }
            }
        }

        //Not worrying about ASCII or EBCDIC or multinational character set.
        //Assuming that the low nibble contains the binary value of the zone.
        //Correct me if I am wrong.
        int i = lastDigit;

        while (data > 0 && i >= firstDigit) {
            byte aByte = (byte) ('0' + (data % 10));
            temp[i] = aByte;//the or takes care of sign
            data = data / 10;
            i--;
        }
        toEbcdicFromAscii(temp, firstDigit, lastDigit - firstDigit + 1);
        if (isSigned && !isSignSeparate && isSignLeading) {
            temp[firstDigit] = (byte) (temp[firstDigit] | (byte) anotherByte);
        }
        return new String(temp, fsiz, len);                 
    }
}
