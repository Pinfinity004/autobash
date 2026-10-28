package com.pratik.imagetap;
import java.util.*;import org.opencv.core.*;import org.opencv.imgproc.Imgproc;
/** Repeatable desktop workload, not a prediction of phone latency. */
public class HybridBenchmark {
 static Object cache;
 static VisionMatcher.Frame frame(int width)throws Exception{
  try{return VisionMatcher.Frame.class.getConstructor(Mat.class,int.class,int.class,List.class,Class.forName("com.pratik.imagetap.VisionMatcher$Cache")).newInstance(scene.clone(),width,96,Collections.emptyList(),cache);}
  catch(ClassNotFoundException e){return new VisionMatcher.Frame(scene.clone(),width,96,Collections.emptyList());}
 }
 static Mat scene;static List<Mat> targets=new ArrayList<>();
 static void setup(boolean masked){
  Random rng=new Random(318);byte[] pixels=new byte[1080*2340*4];
  // Smooth multicolored scenery with textured bands and high contrast game-like clutter.
  for(int y=0;y<2340;y++)for(int x=0;x<1080;x++){int i=(y*1080+x)*4;pixels[i]=(byte)(25+(x/80*23+y/150*13)%140);pixels[i+1]=(byte)(30+(x/120*33+y/90*17)%120);pixels[i+2]=(byte)(20+(x/100*11+y/130*29)%150);pixels[i+3]=(byte)255;}
  scene=new Mat(2340,1080,CvType.CV_8UC4);scene.put(0,0,pixels);
  for(int i=0;i<51;i++){int w=masked?32:72+(i%3)*18,h=masked?28:48+(i%3)*12;Mat t=new Mat(h,w,CvType.CV_8UC4,masked?new Scalar(0,0,0,0):new Scalar(40+i,60+i,80+i,255));
   Imgproc.putText(t,Integer.toString(i),new Point(2,h-7),Imgproc.FONT_HERSHEY_SIMPLEX,masked?.55:.8,new Scalar(240,230,210,255),2);targets.add(t);}
  Mat t=targets.get(50);int x=900,y=180;Mat roi=scene.submat(y,y+t.rows(),x,x+t.cols());Mat alpha=new Mat();Core.extractChannel(t,alpha,3);t.copyTo(roi,alpha);roi.release();alpha.release();
 }
 public static void main(String[] args)throws Exception{nu.pattern.OpenCV.loadLocally();Core.setNumThreads(2);boolean hybrid=args.length==0||!args[0].equals("legacy");
  for(boolean masked:new boolean[]{false,true}){setup(masked);try{cache=Class.forName("com.pratik.imagetap.VisionMatcher$Cache").getConstructor().newInstance();}catch(ClassNotFoundException ignored){}long[] times=new long[5];int hits=0;long mask=0;
   for(int run=0;run<6;run++){long start=System.nanoTime();
    try(VisionMatcher.Frame normal=frame(720);VisionMatcher.Frame fine=frame(1080)){
     try{VisionMatcher.Frame.class.getField("hybrid").setBoolean(normal,hybrid);VisionMatcher.Frame.class.getField("hybrid").setBoolean(fine,hybrid);}catch(NoSuchFieldException ignored){}
     mask=0;for(int i=0;i<51;i++){Mat t=targets.get(i);VisionMatcher.Frame f=Math.min(t.cols(),t.rows())*normal.factor<24?fine:normal;VisionMatcher.Hit hit=VisionMatcher.findExpected(f,t,null,1,null);if(hit!=null)mask|=1L<<i;if(i==50){if(hit==null||Math.abs(hit.x-900)>2||Math.abs(hit.y-180)>2)throw new AssertionError("Target 51 missing: "+masked);hits++;}}
    }
    if(run>0)times[run-1]=(System.nanoTime()-start)/1000000;
   }Arrays.sort(times);System.out.println((hybrid?"hybrid":"legacy")+" "+(masked?"masked tiny":"opaque")+" 51 targets, median="+times[2]+"ms, runs="+Arrays.toString(times)+", target51="+hits+"/6, matchMask="+Long.toHexString(mask));if(cache!=null)((AutoCloseable)cache).close();for(Mat t:targets)t.release();targets.clear();scene.release();
  }
 }
}
