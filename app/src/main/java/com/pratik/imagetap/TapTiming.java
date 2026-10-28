package com.pratik.imagetap;
/** Timing uses elapsed time, never wall-clock time or session/click limits. */
final class TapTiming {
    static final int MIN_CAPTURE_MS=100;
    static int pulse(int value){return Math.max(100,Math.min(10000,value));}
    static int throttledCaptureFloor(int current){return Math.min(2000,Math.max(350,current+100));}
    static int similarity(int value){return Math.max(75,Math.min(100,value));}
    static int press(int value){return Math.max(40,Math.min(250,value));}
    static int delay(int value){return Math.max(0,Math.min(5000,value));}
    static long captureWait(long now,long lastCapture){return Math.max(0,MIN_CAPTURE_MS-(now-lastCapture));}
    static boolean canTap(long now,long lastTap,int delayMs,boolean gesturePending){return !gesturePending&&(lastTap<0||now-lastTap>=delay(delayMs));}
    static long retryWait(int failures){return Math.min(10000,500L*(1L<<Math.min(5,Math.max(0,failures-1))));}
}
