package com.pratik.imagetap;
public class TapTimingTest {
    static void require(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args){
        require(TapTiming.similarity(20)==75,"clamp lower accuracy");require(TapTiming.similarity(100)==100,"100% endpoint");require(TapTiming.similarity(75)==75,"75% endpoint");require(TapTiming.similarity(105)==100,"clamp upper accuracy");
        require(TapTiming.canTap(0,-1,5000,false),"First detection has no unnecessary cooldown");
        require(TapTiming.canTap(1000,1000,0,false),"Zero adds no tap delay");require(!TapTiming.canTap(1000,1000,0,true),"No overlapping gestures");
        require(!TapTiming.canTap(1499,1000,500,false),"Cooldown enforced");require(TapTiming.canTap(1500,1000,500,false),"Exact delay endpoint");
        require(TapTiming.canTap(36000000,35999999,0,false),"No maximum session duration");
        require(TapTiming.captureWait(1000,950)==50,"Respect capture interval");require(TapTiming.captureWait(1300,900)==0,"No extra wait after processing");
        require(TapTiming.pulse(0)==100&&TapTiming.pulse(100)==100&&TapTiming.pulse(12000)==10000,"100ms pulse endpoints");
        require(TapTiming.throttledCaptureFloor(100)==350&&TapTiming.throttledCaptureFloor(350)==450&&TapTiming.throttledCaptureFloor(2000)==2000,"Adaptive screenshot throttle");
        require(TapTiming.retryWait(1)==500,"First retry");require(TapTiming.retryWait(100000)==10000,"Failures keep retrying with bounded backoff");
        require(TapTiming.delay(-100)==0&&TapTiming.delay(10000)==5000,"Delay range");
        System.out.println("PASS: 75–100 accuracy, 0–5000ms delay, first-tap/zero-delay behavior, no overlap, capture pacing, bounded retries");
    }
}
