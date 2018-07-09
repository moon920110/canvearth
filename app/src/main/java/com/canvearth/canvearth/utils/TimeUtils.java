package com.canvearth.canvearth.utils;

import com.canvearth.canvearth.utils.concurrency.Function;

import java.util.concurrent.TimeUnit;

public class TimeUtils {
    public static long measureTimeMillis(Function<Object> function) {
        long startTime = System.nanoTime();
        function.run(null);
        long endTime = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
    }
}
