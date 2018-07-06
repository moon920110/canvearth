package com.canvearth.canvearth.utils;

public class MathUtils {
    public static int intPow(int num, int factor) {
        int result = 1;
        for (int i = 0; i < factor; i++) {
            result *= num;
        }
        return result;
    }
}
