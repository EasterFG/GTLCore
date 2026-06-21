package org.gtlcore.gtlcore.utils.datastructure;

import com.google.common.primitives.Ints;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

/**
 * 高性能128位有符号整数实现，使用两个long组成
 * 避免频繁创建新对象，支持原地修改
 */
@SuppressWarnings({ "unused", "DuplicatedCode", "UnusedReturnValue" })
@Getter
public final class Int128 extends Number implements Comparable<Int128> {

    // 高64位和低64位
    private long high;
    private long low;

    public static Int128 ZERO() {
        return new Int128(0, 0);
    }

    public static Int128 ONE() {
        return new Int128(0, 1);
    }

    public static Int128 NEGATIVE_ONE() {
        return new Int128(-1L, -1L);
    }

    public static final Int128 MAX_VALUE = new Int128(0x7FFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL);
    public static final Int128 MIN_VALUE = new Int128(0x8000000000000000L, 0);

    public Int128() {
        this.high = 0;
        this.low = 0;
    }

    @Override
    public int intValue() {
        return Ints.saturatedCast(longValue());
    }

    @Override
    public long longValue() {
        // 正常范围内的数字
        if ((high == 0 && low >= 0) || (high == -1L && low < 0)) {
            return low;
        }

        // 超出范围，饱和到边界值
        return isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
    }

    @Override
    public float floatValue() {
        return (float) toDouble();
    }

    @Override
    public double doubleValue() {
        return toDouble();
    }

    public Int128(long high, long low) {
        this.high = high;
        this.low = low;
    }

    public Int128(long value) {
        this.high = value < 0 ? -1L : 0;
        this.low = value;
    }

    public Int128 set(long high, long low) {
        this.high = high;
        this.low = low;
        return this;
    }

    public Int128 set(Int128 other) {
        this.high = other.high;
        this.low = other.low;
        return this;
    }

    // =================== 算术运算 ===================

    public Int128 add(long other) {
        long newLow = this.low + other;
        long newHigh = this.high;

        if (Long.compareUnsigned(newLow, this.low) < 0) {
            newHigh++; // 发生无符号进位
        }

        if (other < 0) {
            newHigh--;
        }

        this.low = newLow;
        this.high = newHigh;
        return this;
    }

    public Int128 add(Int128 other) {
        long newLow = this.low + other.low;
        long newHigh = this.high + other.high;

        if (Long.compareUnsigned(newLow, this.low) < 0) {
            newHigh++;
        }

        this.low = newLow;
        this.high = newHigh;
        return this;
    }

    public static Int128 add(Int128 a, Int128 b, Int128 result) {
        long newLow = a.low + b.low;
        long newHigh = a.high + b.high;

        if (Long.compareUnsigned(newLow, a.low) < 0) {
            newHigh++;
        }

        result.low = newLow;
        result.high = newHigh;
        return result;
    }

    public Int128 subtract(Int128 other) {
        long newLow = this.low - other.low;
        long newHigh = this.high - other.high;

        // 处理无符号借位
        if (Long.compareUnsigned(this.low, other.low) < 0) {
            newHigh--;
        }

        this.low = newLow;
        this.high = newHigh;
        return this;
    }

    public static Int128 subtract(Int128 a, Int128 b, Int128 result) {
        long newLow = a.low - b.low;
        long newHigh = a.high - b.high;

        if (Long.compareUnsigned(a.low, b.low) < 0) {
            newHigh--;
        }

        result.low = newLow;
        result.high = newHigh;
        return result;
    }

    public Int128 multiply(Int128 other) {
        long a0 = this.low & 0xFFFFFFFFL;
        long a1 = this.low >>> 32;
        long a2 = this.high & 0xFFFFFFFFL;
        long a3 = this.high >>> 32;

        long b0 = other.low & 0xFFFFFFFFL;
        long b1 = other.low >>> 32;
        long b2 = other.high & 0xFFFFFFFFL;
        long b3 = other.high >>> 32;

        long p0 = a0 * b0;
        long p1 = a0 * b1 + a1 * b0;
        long p2 = a0 * b2 + a1 * b1 + a2 * b0;
        long p3 = a0 * b3 + a1 * b2 + a2 * b1 + a3 * b0;

        p1 += p0 >>> 32;
        p2 += p1 >>> 32;
        p3 += p2 >>> 32;

        this.low = (p1 << 32) | (p0 & 0xFFFFFFFFL);
        this.high = (p3 << 32) | (p2 & 0xFFFFFFFFL);

        return this;
    }

    public static Int128 multiply(Int128 a, Int128 b, Int128 result) {
        long a0 = a.low & 0xFFFFFFFFL;
        long a1 = a.low >>> 32;
        long a2 = a.high & 0xFFFFFFFFL;
        long a3 = a.high >>> 32;

        long b0 = b.low & 0xFFFFFFFFL;
        long b1 = b.low >>> 32;
        long b2 = b.high & 0xFFFFFFFFL;
        long b3 = b.high >>> 32;

        long p0 = a0 * b0;
        long p1 = a0 * b1 + a1 * b0;
        long p2 = a0 * b2 + a1 * b1 + a2 * b0;
        long p3 = a0 * b3 + a1 * b2 + a2 * b1 + a3 * b0;

        p1 += p0 >>> 32;
        p2 += p1 >>> 32;
        p3 += p2 >>> 32;

        result.low = (p1 << 32) | (p0 & 0xFFFFFFFFL);
        result.high = (p3 << 32) | (p2 & 0xFFFFFFFFL);

        return result;
    }

    public Int128 multiply(long multiplier) {
        long a0 = this.low & 0xFFFFFFFFL;
        long a1 = this.low >>> 32;
        long a2 = this.high & 0xFFFFFFFFL;
        long a3 = this.high >>> 32;

        long m0 = multiplier & 0xFFFFFFFFL;
        long m1 = multiplier >>> 32;

        long p0 = a0 * m0;
        long p1 = a0 * m1 + a1 * m0;
        long p2 = a1 * m1 + a2 * m0;
        long p3 = a2 * m1 + a3 * m0;

        p1 += p0 >>> 32;
        p2 += p1 >>> 32;
        p3 += p2 >>> 32;

        this.low = (p1 << 32) | (p0 & 0xFFFFFFFFL);
        this.high = (p3 << 32) | (p2 & 0xFFFFFFFFL);

        return this;
    }

    public static Int128 multiply(Int128 a, long multiplier, Int128 result) {
        result.set(a.high, a.low);
        return result.multiply(multiplier);
    }

    public Int128 divide(Int128 divisor, Int128 remainder) {
        if (divisor.isZero()) {
            throw new ArithmeticException("Division by zero");
        }

        if (this.isZero()) {
            remainder.set(0, 0);
            return this.set(0, 0);
        }

        boolean negativeResult = (this.isNegative() != divisor.isNegative());

        Int128 dividend = new Int128(this.high, this.low);
        Int128 div = new Int128(divisor.high, divisor.low);

        if (dividend.isNegative()) dividend.negate();
        if (div.isNegative()) div.negate();

        Int128 quotient = new Int128();
        Int128 temp = new Int128();

        for (int i = 127; i >= 0; i--) {
            temp.shiftLeft(1);
            if (dividend.getBit(i)) {
                temp.low |= 1;
            }

            if (temp.compareTo(div) >= 0) {
                temp.subtract(div);
                quotient.setBit(i, true);
            }
        }

        remainder.set(temp);

        this.set(quotient);
        if (negativeResult) {
            this.negate();
        }

        return this;
    }

    public Int128 divide(long divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Division by zero");
        }

        boolean neg = (this.isNegative() != (divisor < 0));

        if (this.isNegative()) this.negate();
        if (divisor < 0) divisor = -divisor;

        long rem = 0;
        long resultHigh = 0;
        long resultLow = 0;

        if (high != 0) {
            resultHigh = Long.divideUnsigned(high, divisor);
            rem = Long.remainderUnsigned(high, divisor);
        }

        if (rem != 0) {
            long combined = (rem << 32) | (low >>> 32);
            long q1 = Long.divideUnsigned(combined, divisor);
            rem = Long.remainderUnsigned(combined, divisor);

            combined = (rem << 32) | (low & 0xFFFFFFFFL);
            long q0 = Long.divideUnsigned(combined, divisor);

            resultLow = (q1 << 32) | q0;
        } else {
            resultLow = Long.divideUnsigned(low, divisor);
        }

        this.high = resultHigh;
        this.low = resultLow;

        if (neg) this.negate();

        return this;
    }

    public Int128 divideNew(long divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Division by zero");
        }

        // 创建副本进行计算
        Int128 result = new Int128(this.high, this.low);
        boolean neg = (result.isNegative() != (divisor < 0));

        if (result.isNegative()) result.negate();
        long absDivisor = divisor < 0 ? -divisor : divisor;

        long rem = 0;
        long resultHigh = 0;
        long resultLow = 0;

        if (result.high != 0) {
            resultHigh = Long.divideUnsigned(result.high, absDivisor);
            rem = Long.remainderUnsigned(result.high, absDivisor);
        }

        if (rem != 0) {
            long combined = (rem << 32) | (result.low >>> 32);
            long q1 = Long.divideUnsigned(combined, absDivisor);
            rem = Long.remainderUnsigned(combined, absDivisor);

            combined = (rem << 32) | (result.low & 0xFFFFFFFFL);
            long q0 = Long.divideUnsigned(combined, absDivisor);

            resultLow = (q1 << 32) | q0;
        } else {
            resultLow = Long.divideUnsigned(result.low, absDivisor);
        }

        result.high = resultHigh;
        result.low = resultLow;

        if (neg) result.negate();

        return result;
    }

    // =================== 位运算 ===================

    public Int128 shiftLeft(int n) {
        n &= 127; // 限制在0-127范围

        if (n >= 64) {
            this.high = this.low << (n - 64);
            this.low = 0;
        } else if (n > 0) {
            this.high = (this.high << n) | (this.low >>> (64 - n));
            this.low = this.low << n;
        }

        return this;
    }

    public Int128 shiftRight(int n) {
        n &= 127;

        if (n >= 64) {
            this.low = this.high >> (n - 64);
            this.high = this.high >> 63; // 符号扩展
        } else if (n > 0) {
            this.low = (this.low >>> n) | (this.high << (64 - n));
            this.high = this.high >> n;
        }

        return this;
    }

    public Int128 shiftRightUnsigned(int n) {
        n &= 127;

        if (n >= 64) {
            this.low = this.high >>> (n - 64);
            this.high = 0;
        } else if (n > 0) {
            this.low = (this.low >>> n) | (this.high << (64 - n));
            this.high = this.high >>> n;
        }

        return this;
    }

    public Int128 negate() {
        this.low = ~this.low;
        this.high = ~this.high;

        // 加1
        this.low++;
        if (this.low == 0) {
            this.high++;
        }

        return this;
    }

    // =================== 比较运算 ===================

    @Override
    public int compareTo(Int128 other) {
        boolean thisNeg = this.isNegative();
        boolean otherNeg = other.isNegative();

        if (thisNeg != otherNeg) {
            return thisNeg ? -1 : 1;
        }

        if (this.high != other.high) {
            return Long.compare(this.high, other.high);
        }

        return Long.compareUnsigned(this.low, other.low);
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Int128 other)) return false;
        return this.high == other.high && this.low == other.low;
    }

    // =================== 工具方法 ===================

    public boolean isZero() {
        return high == 0 && low == 0;
    }

    public boolean isNegative() {
        return high < 0;
    }

    public boolean isPositive() {
        return !isNegative() && !isZero();
    }

    public boolean getBit(int index) {
        if (index < 64) {
            return (low & (1L << index)) != 0;
        } else {
            return (high & (1L << (index - 64))) != 0;
        }
    }

    public void setBit(int index, boolean value) {
        if (index < 64) {
            if (value) {
                low |= (1L << index);
            } else {
                low &= ~(1L << index);
            }
        } else {
            if (value) {
                high |= (1L << (index - 64));
            } else {
                high &= ~(1L << (index - 64));
            }
        }
    }

    public long toLong() {
        return low;
    }

    public double toDouble() {
        return high * Math.pow(2, 64) + (low & 0x7FFFFFFFFFFFFFFFL) + (low < 0 ? Math.pow(2, 63) : 0);
    }

    public BigInteger toBigInteger() {
        if (isZero()) {
            return BigInteger.ZERO;
        }

        byte[] bytes = new byte[16];

        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (high >>> (56 - i * 8));
        }

        for (int i = 0; i < 8; i++) {
            bytes[i + 8] = (byte) (low >>> (56 - i * 8));
        }

        return new BigInteger(bytes);
    }

    public static Int128 fromBigInteger(BigInteger value) {
        if (value == null) {
            return ZERO();
        }

        if (value.bitLength() > 127) {
            throw new ArithmeticException("BigInteger too large for Int128: " + value);
        }

        if (value.equals(BigInteger.ZERO)) {
            return new Int128(0, 0);
        }
        if (value.equals(BigInteger.ONE)) {
            return new Int128(0, 1);
        }

        byte[] bytes = value.toByteArray();

        long high = 0, low = 0;

        int len = bytes.length;

        for (int i = 0; i < Math.min(8, len); i++) {
            int byteIndex = len - 1 - i;
            if (byteIndex >= 0) {
                low |= ((long) (bytes[byteIndex] & 0xFF)) << (i * 8);
            }
        }

        for (int i = 8; i < Math.min(16, len); i++) {
            int byteIndex = len - 1 - i;
            if (byteIndex >= 0) {
                high |= ((long) (bytes[byteIndex] & 0xFF)) << ((i - 8) * 8);
            }
        }

        if (value.signum() < 0 && len < 16) {
            if (len <= 8) {
                if (len < 8) {
                    low |= (-1L << (len * 8));
                }
                high = -1L;
            } else {
                high |= (-1L << ((len - 8) * 8));
            }
        }

        return new Int128(high, low);
    }

    @Override
    public String toString() {
        if (isZero()) return "0";

        if (high == 0 || (high == -1 && low < 0)) {
            return Long.toString(low);
        }

        return toStringFast();
    }

    private String toStringFast() {
        boolean negative = isNegative();

        long workHigh = negative ? ~high : high;
        long workLow = negative ? ~low + 1 : low;
        if (negative && workLow == 0) workHigh++;

        if (workHigh == 0) {
            return negative ? "-" + workLow : Long.toString(workLow);
        }

        char[] digits = new char[40];
        int pos = digits.length;

        final long BILLION = 1_000_000_000L;

        while (workHigh != 0 || workLow != 0) {
            long quotientHigh, quotientLow, remainder;

            if (workHigh == 0) {
                quotientHigh = 0;
                quotientLow = workLow / BILLION;
                remainder = workLow % BILLION;
            } else {
                long temp = workHigh % BILLION;
                quotientHigh = workHigh / BILLION;

                long combined = (temp << 32) | (workLow >>> 32);
                long q1 = combined / BILLION;
                temp = combined % BILLION;

                combined = (temp << 32) | (workLow & 0xFFFFFFFFL);
                long q0 = combined / BILLION;
                remainder = combined % BILLION;

                quotientLow = (q1 << 32) | q0;
            }

            for (int i = 0; i < 9 && (remainder != 0 || workHigh != 0 || workLow != quotientLow * BILLION + remainder); i++) {
                digits[--pos] = (char) ('0' + (remainder % 10));
                remainder /= 10;
            }

            workHigh = quotientHigh;
            workLow = quotientLow;
        }

        if (negative) {
            digits[--pos] = '-';
        }

        return new String(digits, pos, digits.length - pos);
    }

    public static Int128 fromString(@NotNull String str) {
        str = str.trim();
        if (str.isEmpty()) {
            throw new NumberFormatException("empty string");
        }

        return fromDecimalString(str);
    }

    public static Int128 fromString(@NotNull String str, Int128 defaultValue) {
        try {
            return fromString(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static Int128 fromDecimalString(@NotNull String str) {
        boolean negative = false;
        int start = 0;

        if (str.charAt(0) == '-') {
            negative = true;
            start = 1;
        } else if (str.charAt(0) == '+') {
            start = 1;
        }

        if (start >= str.length()) {
            throw new NumberFormatException("no digits");
        }

        if (str.length() - start <= 18) {
            try {
                long value = Long.parseLong(str.substring(start));
                return new Int128(negative ? -value : value);
            } catch (NumberFormatException ignored) {}
        }

        Int128 result = new Int128();
        Int128 ten = new Int128(10);

        for (int i = start; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                throw new NumberFormatException("invalid digit: " + c);
            }

            // result = result * 10 + digit
            result.multiply(ten);
            result.add(new Int128(c - '0'));
        }

        if (negative) {
            result.negate();
        }

        return result;
    }

    public static Int128 sum(Int128 a, Int128 b) {
        return a.add(b);
    }

    public String toHexString() {
        return String.format("%016X%016X", high, low);
    }

    /**
     * 格式化数字的toString版本，使用千分位分隔符
     * 
     * @param separator 分隔符，通常为 "," 或 " "
     * @return 格式化后的字符串
     */
    public String toFormattedString(String separator) {
        if (separator == null) separator = ",";

        String baseStr = this.toString();
        if (baseStr.length() <= 3) {
            return baseStr;
        }

        boolean negative = baseStr.startsWith("-");
        String digits = negative ? baseStr.substring(1) : baseStr;

        StringBuilder formatted = new StringBuilder();
        int len = digits.length();

        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) {
                formatted.append(separator);
            }
            formatted.append(digits.charAt(i));
        }

        if (negative) {
            formatted.insert(0, "-");
        }

        return formatted.toString();
    }

    public String toFormattedString() {
        return toFormattedString(",");
    }

    /**
     * 紧凑格式，使用科学计数法显示大数字
     * 
     * @return 紧凑格式的字符串，如 "1.23E+15"
     */
    public String toCompactString() {
        if (isZero()) return "0";

        String str = this.toString();
        boolean negative = str.startsWith("-");
        String digits = negative ? str.substring(1) : str;

        if (digits.length() <= 6) {
            return str;
        }

        char firstDigit = digits.charAt(0);

        String mantissa = String.valueOf(firstDigit) + '.' + digits.substring(1, 4);

        int exponent = digits.length() - 1;
        String result = mantissa + "E+" + exponent;

        return negative ? "-" + result : result;
    }

    /**
     * 人类可读的格式，使用单位后缀
     * 
     * @return 人类可读的字符串，如 "1.23K", "4.56M", "7.89B"
     */
    public String toHumanReadableString() {
        if (isZero()) return "0";

        String[] units = { "", "K", "M", "B", "T", "P", "E", "Z", "Y" };

        String str = this.toString();
        boolean negative = str.startsWith("-");
        String digits = negative ? str.substring(1) : str;

        if (digits.length() <= 3) {
            return str;
        }

        int unitIndex = (digits.length() - 1) / 3;
        if (unitIndex >= units.length) {
            return toCompactString();
        }

        int significantDigits = digits.length() - (unitIndex * 3);
        String integerPart = digits.substring(0, significantDigits);

        StringBuilder result = new StringBuilder();
        if (negative) result.append("-");

        result.append(integerPart);

        int remainingDigits = digits.length() - significantDigits;
        if (remainingDigits > 0 && integerPart.length() < 3) {
            result.append(".");
            int decimalPlaces = Math.min(2, Math.min(remainingDigits, 3 - integerPart.length()));
            result.append(digits, significantDigits, significantDigits + decimalPlaces);
        }

        result.append(units[unitIndex]);

        return result.toString();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(high) * 31 + Long.hashCode(low);
    }

    public Int128 copy() {
        return new Int128(this.high, this.low);
    }
}
