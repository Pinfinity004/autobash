package com.pratik.imagetap;
import java.util.*;
public class ReviewRegressionTest {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 public static void main(String[] args){
  TargetSchedule s=new TargetSchedule();Set<Integer> seen=new HashSet<>();for(int i=0;i<200;i++){int[] batch=s.next(20,0,4);seen.add(batch[0]);s.defer(batch[1]);}check(seen.size()==20,"Budget-limited priority scans starve targets: "+seen);System.out.println("PASS priority+budget coverage: "+seen.size()+"/20");
  CaptureGate gate=new CaptureGate();int first=gate.begin();check(first!=0&&gate.begin()==0,"Overlapping capture admitted");check(!gate.finish(first+1)&&gate.busy(),"Wrong callback released capture");check(gate.finish(first)&&!gate.busy(),"Capture not released");int second=gate.begin();gate.reset();int third=gate.begin();check(!gate.finish(second)&&gate.busy(),"Stale callback released new generation");check(gate.finish(third),"New generation cannot finish");
  check(CaptureGate.fresh(3000,1000),"Boundary rejected");check(!CaptureGate.fresh(3001,1000),"Old screenshot admitted");check(!CaptureGate.fresh(10,11),"Future timestamp admitted");check(!CaptureGate.fresh(10,-1),"Invalid timestamp admitted");System.out.println("PASS capture ownership and screenshot-age boundaries");
 }
}
