package com.pratik.imagetap;
import org.opencv.core.*;import org.opencv.imgproc.Imgproc;import java.util.*;
public class FullFrameScanTest {
 public static void main(String[] args){nu.pattern.OpenCV.loadLocally();Core.setNumThreads(2);Mat original=new Mat(1560,720,CvType.CV_8UC4,new Scalar(25,35,45,255));List<Mat> targets=new ArrayList<>();
 for(int i=0;i<51;i++){Mat t=new Mat(24,32,CvType.CV_8UC4,new Scalar(10+i,20,30,255));Imgproc.putText(t,Integer.toString(i),new Point(1,19),Imgproc.FONT_HERSHEY_SIMPLEX,.55,new Scalar(240,220,190,255),1);targets.add(t);}
 Mat roi=original.submat(120,144,600,632);targets.get(50).copyTo(roi);roi.release();long start=System.nanoTime();
 try(VisionMatcher.Frame f=new VisionMatcher.Frame(original,720,100,Collections.emptyList())){int checked=0;VisionMatcher.Hit last=null;for(Mat t:targets){last=VisionMatcher.findExpected(f,t,null,1,null);checked++;}if(checked!=51||last==null||last.x!=600||last.y!=120)throw new AssertionError("Last target must see retained frame");System.out.println("PASS all 51 expected-size checks against one 720x1560 frame; last target found; desktop ms="+(System.nanoTime()-start)/1000000);f.cancelled=()->true;if(VisionMatcher.findExpected(f,targets.get(50),null,1,null)!=null)throw new AssertionError("Pause must cancel matching");if(VisionMatcher.find(f,targets.get(50),null,Long.MAX_VALUE)!=null)throw new AssertionError("Pause must cancel alternate scales");System.out.println("PASS cooperative cancellation in both matcher passes");}for(Mat t:targets)t.release();
 }
}
