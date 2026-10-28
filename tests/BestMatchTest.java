package com.pratik.imagetap;
import org.opencv.core.*;import org.opencv.imgproc.Imgproc;import java.util.*;
public class BestMatchTest {
 public static void main(String[] args){nu.pattern.OpenCV.loadLocally();Core.setNumThreads(2);Mat template=new Mat(48,72,CvType.CV_8UC4,new Scalar(45,60,90,255));Imgproc.putText(template,"EXIT",new Point(2,34),Imgproc.FONT_HERSHEY_SIMPLEX,.65,new Scalar(245,230,220,255),2);
  for(boolean hybrid:new boolean[]{false,true}){Mat screen=new Mat(1280,720,CvType.CV_8UC4,new Scalar(20,25,35,255));Mat weak=new Mat();template.convertTo(weak,-1,.92,0);Mat left=screen.submat(200,248,80,152),right=screen.submat(700,748,500,572);weak.copyTo(left);template.copyTo(right);left.release();right.release();weak.release();
   try(VisionMatcher.Frame f=new VisionMatcher.Frame(screen,720,90,Collections.emptyList())){f.hybrid=hybrid;VisionMatcher.Hit previous=new VisionMatcher.Hit(80,200,72,48,95,90);VisionMatcher.Hit h=VisionMatcher.findExpected(f,template,previous,1,null);if(h==null||h.x!=500||h.y!=700)throw new AssertionError("Stronger distant instance must beat tracked weak instance, hybrid="+hybrid);}
  }template.release();System.out.println("PASS strongest instance beats tracked weaker duplicate in hybrid and direct search");
 }
}
