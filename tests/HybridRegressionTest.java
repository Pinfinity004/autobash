package com.pratik.imagetap;
import org.opencv.core.*;import org.opencv.imgproc.Imgproc;import java.util.*;
public class HybridRegressionTest {
 static void require(boolean ok,String s){if(!ok)throw new AssertionError(s);}
 static void composite(Mat screen,Mat t,int x,int y){Mat a=new Mat(),r=screen.submat(y,y+t.rows(),x,x+t.cols());Core.extractChannel(t,a,3);t.copyTo(r,a);a.release();r.release();}
 public static void main(String[] args){nu.pattern.OpenCV.loadLocally();Core.setNumThreads(2);int cases=0;
  Mat cross=new Mat(28,32,CvType.CV_8UC4,new Scalar(0,0,0,0));Imgproc.line(cross,new Point(5,4),new Point(25,23),new Scalar(245,245,245,255),3);Imgproc.line(cross,new Point(25,4),new Point(5,23),new Scalar(245,245,245,255),3);
  Mat opaque=new Mat(48,72,CvType.CV_8UC4,new Scalar(45,60,90,255));Imgproc.putText(opaque,"EXIT",new Point(2,34),Imgproc.FONT_HERSHEY_SIMPLEX,.65,new Scalar(245,230,220,255),2);
  try(VisionMatcher.Cache cache=new VisionMatcher.Cache()){
   for(Mat t:new Mat[]{cross,opaque})for(int phase=0;phase<4;phase++)for(int accuracy:new int[]{90,100}){
    Mat screen=new Mat(1560,720,CvType.CV_8UC4);byte[] b=new byte[720*1560*4];Random rng=new Random(42+phase);
    for(int i=0;i<b.length;i+=4){b[i]=(byte)rng.nextInt(170);b[i+1]=(byte)rng.nextInt(130);b[i+2]=(byte)rng.nextInt(100);b[i+3]=(byte)255;}screen.put(0,0,b);
    int x=421+phase,y=93+phase;composite(screen,t,x,y);
    try(VisionMatcher.Frame f=new VisionMatcher.Frame(screen,720,accuracy,Collections.emptyList(),cache)){
     VisionMatcher.Hit h=VisionMatcher.findExpected(f,t,null,1,null);require(h!=null&&Math.abs(h.x-x)<=1&&Math.abs(h.y-y)<=1,"Hybrid miss type="+(t==cross?"mask":"opaque")+" phase="+phase+" accuracy="+accuracy);require(f.proposalSearches>0,"Hybrid path used");cases++;
    }
   }
   Mat screen=new Mat(1560,720,CvType.CV_8UC4,new Scalar(25,30,40,255));composite(screen,cross,520,100);
   try(VisionMatcher.Frame f=new VisionMatcher.Frame(screen,720,90,Arrays.asList(new RegionRules.Region(.7f,0,1,.2f,90,true)),cache)){require(VisionMatcher.findExpected(f,cross,null,1,null)==null,"Excluded target must not match");}
   screen=new Mat(1560,720,CvType.CV_8UC4,new Scalar(245,245,245,255));try(VisionMatcher.Frame f=new VisionMatcher.Frame(screen,720,90,Collections.emptyList(),cache)){require(VisionMatcher.findExpected(f,cross,null,1,null)==null,"Flat white cannot trigger masked cross");}
   screen=new Mat(1560,720,CvType.CV_8UC4,new Scalar(25,30,40,255));composite(screen,opaque,421,93);
   try(VisionMatcher.Frame f=new VisionMatcher.Frame(screen,720,100,Collections.emptyList(),cache)){require(VisionMatcher.findExpected(f,opaque,null,1,new RegionRules.Region(0,.5f,1,1,90))==null,"Per-target zone");f.cancelled=()->true;require(VisionMatcher.findExpected(f,opaque,null,1,null)==null,"Cancel");}
   cache.clear();
  }cross.release();opaque.release();System.out.println("PASS hybrid: "+cases+" placements on textured backgrounds, exact mode, exclusions, per-target zone, flat distractor, cancellation, cache lifetime");
 }
}
