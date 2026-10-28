package com.pratik.imagetap;
/** Main-thread capture ownership. A stale callback cannot release a newer request. */
final class CaptureGate {
 private int next,active;
 int begin(){if(active!=0)return 0;active=++next;return active;}
 boolean finish(int id){if(active!=id||id==0)return false;active=0;return true;}
 boolean busy(){return active!=0;}
 void reset(){active=0;next++;}
 static boolean fresh(long now,long captured){return captured>=0&&now>=captured&&now-captured<=2000;}
}
