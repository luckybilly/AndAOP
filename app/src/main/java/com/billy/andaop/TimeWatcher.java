package com.billy.andaop;

import java.text.NumberFormat;

/**
 * 计时器
 * @author billy.qi
 * @since 16/12/2 16:44
 */
public class TimeWatcher {
    private NumberFormat numberFormat;
    private long startTime;
    private long endTime;
    private long elapsedTime;

    public TimeWatcher() {
        numberFormat = NumberFormat.getNumberInstance();
    }


    public void reset() {
        startTime = 0;
        endTime = 0;
        elapsedTime = 0;
    }

    public void start() {
        reset();
        startTime = System.nanoTime();
    }

    public void stop() {
        if (startTime != 0) {
            endTime = System.nanoTime();
            elapsedTime = endTime - startTime;
        } else {
            reset();
        }
    }

    public long getTotalTimeAsNano(){
        return elapsedTime;
    }

    public long getTotalTimeAsMillis() {
        return elapsedTime / 1000000;
    }

    public String getTotalTimeAsString() {
        long ms =elapsedTime / 1000000;
        if (ms > 0) {
            return ms + " ms";
        } else {
            long ns = elapsedTime % 1000000;
            String format = numberFormat.format(ns);
            return format + " ns";
        }
    }
}
