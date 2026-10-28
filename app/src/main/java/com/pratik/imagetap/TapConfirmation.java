package com.pratik.imagetap;
import java.util.HashMap;
/** Independent evidence for each target. The same captured frame cannot confirm itself. */
final class TapConfirmation {
 private static final class Seen {long frame;int x,y,count;}
 private final HashMap<Integer,Seen> seen=new HashMap<>();
 int observe(int target,int x,int y,long frame){Seen s=seen.get(target);if(s!=null&&s.frame==frame)return s.count;int count=s!=null&&frame>s.frame&&Math.abs(x-s.x)<18&&Math.abs(y-s.y)<18?Math.min(2,s.count+1):1;if(s==null){s=new Seen();seen.put(target,s);}s.frame=frame;s.x=x;s.y=y;s.count=count;return count;}
 void reset(int target){seen.remove(target);}
 void clear(){seen.clear();}
}
