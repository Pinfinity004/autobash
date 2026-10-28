package com.pratik.imagetap;
import java.util.*;import org.opencv.core.*;import org.opencv.imgproc.Imgproc;
/** Small-symbol phase/size parity against the exhaustive verifier. */
public class HybridParityTest {
 public static void main(String[] args){nu.pattern.OpenCV.loadLocally();Core.setNumThreads(2);int checked=0,found=0;
  for(int size:new int[]{16,20,24,32,48})for(int stroke:new int[]{1,3})for(int phase=0;phase<3;phase++){
   Mat t=new Mat(size,size,CvType.CV_8UC4,new Scalar(0,0,0,0));Imgproc.line(t,new Point(3,3),new Point(size-4,size-4),new Scalar(245,245,245,255),stroke);Imgproc.line(t,new Point(size-4,3),new Point(3,size-4),new Scalar(245,245,245,255),stroke);
   Mat s=new Mat(960,720,CvType.CV_8UC4);byte[] pixels=new byte[720*960*4];Random r=new Random(73+phase);
   for(int i=0;i<pixels.length;i+=4){pixels[i]=(byte)(20+r.nextInt(160));pixels[i+1]=(byte)(20+r.nextInt(130));pixels[i+2]=(byte)(20+r.nextInt(120));pixels[i+3]=(byte)255;}s.put(0,0,pixels);int x=291+phase,y=101+phase;HybridRegressionTest.composite(s,t,x,y);
   try(VisionMatcher.Frame old=new VisionMatcher.Frame(s.clone(),720,100,Collections.emptyList());VisionMatcher.Frame fast=new VisionMatcher.Frame(s,720,100,Collections.emptyList())){
    old.hybrid=false;VisionMatcher.Hit a=VisionMatcher.findExpected(old,t,null,1,null),b=VisionMatcher.findExpected(fast,t,null,1,null);
    if(a!=null){found++;if(b==null||b.x!=a.x||b.y!=a.y)throw new AssertionError("Parity failure size="+size+" stroke="+stroke+" phase="+phase);}
    if(b!=null&&(b.x!=x||b.y!=y))throw new AssertionError("Wrong position");checked++;
   }t.release();
  }System.out.println("PASS hybrid parity "+checked+" cases; "+found+" exhaustive hits preserved (16–48px masked crosses, 1/3px strokes, subscale phases)");
 }
}
