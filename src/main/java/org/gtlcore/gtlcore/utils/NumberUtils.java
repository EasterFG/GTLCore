package org.gtlcore.gtlcore.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.google.common.math.LongMath;

import java.math.BigInteger;
import java.text.DecimalFormat;

public class NumberUtils {

    private static final double LN_095 = Math.log(0.95);

    private static final double[] TABLE = new double[128];

    public static final String[] UNITS = { "", "K", "M", "G", "T", "P", "E", "Z", "Y", "B", "N", "D" };

    public static final int[] NEAREST = { 1, 2, 4, 4, 4, 8, 8, 8, 8, 8, 8, 16, 16, 16, 16, 16 };

    public static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    static {
        double p = 1.0;
        for (int i = 0; i < TABLE.length; i++) {
            TABLE[i] = p;
            p *= 0.95;
        }
    }

    public static double pow95(int n) {
        return (n < TABLE.length) ? TABLE[n] : Math.exp(n * LN_095);
    }

    public static String formatLong(long number) {
        DecimalFormat df = new DecimalFormat("#.##");
        double temp = number;
        int unitIndex = 0;
        while (temp >= 1000 && unitIndex < UNITS.length - 1) {
            temp /= 1000;
            unitIndex++;
        }
        return df.format(temp) + UNITS[unitIndex];
    }

    public static String formatDouble(double number) {
        DecimalFormat df = new DecimalFormat("#.##");
        double temp = number;
        int unitIndex = 0;
        while (temp >= 1000 && unitIndex < UNITS.length - 1) {
            temp /= 1000;
            unitIndex++;
        }
        return df.format(temp) + UNITS[unitIndex];
    }

    public static MutableComponent numberText(double number) {
        return Component.literal(formatDouble(number));
    }

    public static MutableComponent numberText(long number) {
        return Component.literal(formatLong(number));
    }

    public static long getLongValue(BigInteger bigInt) {
        return bigInt.compareTo(BIG_INTEGER_MAX_LONG) > 0 ? Long.MAX_VALUE : bigInt.longValue();
    }

    public static int getFakeVoltageTier(long voltage) {
        return voltage < 32L ? 0 : 1 + ((Long.SIZE - 1 - Long.numberOfLeadingZeros(voltage >> 5)) >> 1);
    }

    public static long getVoltageFromFakeTier(int tier) {
        return LongMath.pow(4L, tier + 1) * 2;
    }

    public static int nearestPow2Lookup(int x) {
        if (x < 1) return 1;
        if (x > 16) return 16;
        return NEAREST[x - 1];
    }

    // actualConsumeParallel > 1
    public static int getAdditionalTier(double durationFactor, double actualConsumeParallel) {
        // 移除边界检查以提升性能
        // if (!(durationFactor > 0.0 && durationFactor < 1.0)) {
        // throw new IllegalArgumentException("require 0 < durationFactor < 1");
        // }

        return (int) Math.ceil(Math.log(actualConsumeParallel) / (-Math.log(durationFactor)));
    }

    // ========================================
    // Saturate
    // ========================================

    public static long saturatedAdd(long a, long b) {
        long naiveSum = a + b;
        if ((a ^ b) < 0 | (a ^ naiveSum) >= 0) {
            // If a and b have different signs or a has the same sign as the result then there was no
            // overflow, return.
            return naiveSum;
        }
        // we did over/under flow, if the sign is negative we should return MAX otherwise MIN
        return Long.MAX_VALUE + ((naiveSum >>> (Long.SIZE - 1)) ^ 1);
    }

    public static long saturatedMultiply(long a, long b) {
        int leadingZeros = Long.numberOfLeadingZeros(a) + Long.numberOfLeadingZeros(~a) + Long.numberOfLeadingZeros(b) + Long.numberOfLeadingZeros(~b);
        if (leadingZeros > Long.SIZE + 1) {
            return a * b;
        }
        // the return value if we will overflow (which we calculate by overflowing a long :) )
        long limit = Long.MAX_VALUE + ((a ^ b) >>> (Long.SIZE - 1));
        if (leadingZeros < Long.SIZE | (a < 0 & b == Long.MIN_VALUE)) {
            // overflow
            return limit;
        }
        long result = a * b;
        if (a == 0 || result / a == b) {
            return result;
        }
        return limit;
    }
}
