package com.pratik.imagetap;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.*;
public class VisionMatcherTest {
    static void require(boolean b,String message){if(!b)throw new AssertionError(message);}
    static Mat target(String text){Mat m=new Mat(40,56,CvType.CV_8UC4,new Scalar(50,70,95,255));Imgproc.rectangle(m,new Point(1,1),new Point(54,38),new Scalar(130,190,80,255),2);Imgproc.putText(m,text,new Point(6,29),Imgproc.FONT_HERSHEY_SIMPLEX,.65,new Scalar(240,225,200,255),2);return m;}
    static Mat screen(){return new Mat(360,240,CvType.CV_8UC4,new Scalar(20,24,29,255));}
    static void insert(Mat frame,Mat patch,int x,int y){Mat roi=frame.submat(y,y+patch.rows(),x,x+patch.cols());patch.copyTo(roi);roi.release();}
    static VisionMatcher.Hit find(Mat frame,Mat target,int accuracy,List<RegionRules.Region> areas){try(VisionMatcher.Frame f=new VisionMatcher.Frame(frame,720,accuracy,areas)){return VisionMatcher.find(f,target,null,System.nanoTime()+4000000000L);}}
    public static void main(String[] args){
        nu.pattern.OpenCV.loadLocally();Core.setNumThreads(2);List<RegionRules.Region> none=Collections.emptyList();Mat t=target("A3");
        Mat s=screen();insert(s,t,71,143);VisionMatcher.Hit h=find(s,t,100,none);require(h!=null&&h.x==71&&h.y==143,"Exact original-resolution placement at 100%");
        Mat bright=t.clone();Core.add(bright,new Scalar(15,15,15,0),bright);s=screen();insert(s,bright,80,120);h=find(s,t,94,none);require(h!=null&&Math.abs(h.x-80)<=1,"Brightness tolerance via normalized correlation");
        s=screen();insert(s,bright,80,120);require(find(s,t,100,none)==null,"100% rejects a brightness near-match");
        Mat onePixel=t.clone();byte[] changed=new byte[4];onePixel.get(2,2,changed);changed[2]^=1;onePixel.put(2,2,changed);s=screen();insert(s,onePixel,71,143);require(find(s,t,100,none)==null,"100% detects even a one-pixel difference");onePixel.release();
        Mat scaled=new Mat();Imgproc.resize(t,scaled,new Size(62,44),0,0,Imgproc.INTER_LINEAR);s=screen();insert(s,scaled,100,180);h=find(s,t,92,none);require(h!=null&&Math.abs(h.x-100)<=2,"Scale variation recognition");
        s=screen();require(find(s,t,75,none)==null,"Uniform background must not match even at 75%");
        Mat wrongColor=t.clone();byte[] colors=new byte[(int)wrongColor.total()*4];wrongColor.get(0,0,colors);for(int i=0;i<colors.length;i+=4){int red=colors[i]&255,green=colors[i+1]&255,blue=colors[i+2]&255;if(red<220&&green<220&&blue<220){colors[i]=(byte)Math.min(255,red+140);colors[i+1]=(byte)Math.max(0,green-45);colors[i+2]=(byte)Math.max(0,blue-65);}}wrongColor.put(0,0,colors);s=screen();insert(s,wrongColor,71,143);require(find(s,t,75,none)==null,"Wrong color must not trigger just because its area is similar");wrongColor.release();
        Mat wrong=target("B8");s=screen();insert(s,wrong,71,143);require(find(s,t,96,none)==null,"Same palette / wrong text distractor rejected");
        s=screen();insert(s,t,71,143);require(find(s,t,90,Arrays.asList(new RegionRules.Region(.2f,.35f,.4f,.6f,90,true)))==null,"Excluded area rejects intersecting candidate, not just center");
        s=screen();insert(s,bright,70,10);insert(s,bright,70,180);h=find(s,t,96,Arrays.asList(new RegionRules.Region(0,0,1,.3f,90),new RegionRules.Region(0,.4f,1,.8f,100)));require(h!=null&&h.y<100,"Regional strictness rejects middle near-match but accepts top");
        Mat alpha=new Mat(40,56,CvType.CV_8UC4,new Scalar(0,0,0,0));Imgproc.putText(alpha,"Q7",new Point(3,30),Imgproc.FONT_HERSHEY_SIMPLEX,.75,new Scalar(255,255,255,255),2);
        s=screen();byte[] ap=new byte[40*56*4];alpha.get(0,0,ap);Mat composite=new Mat(40,56,CvType.CV_8UC4,new Scalar(20,24,29,255));byte[] cp=new byte[ap.length];composite.get(0,0,cp);for(int i=0;i<40*56;i++)if((ap[i*4+3]&255)>32)for(int c=0;c<4;c++)cp[i*4+c]=ap[i*4+c];composite.put(0,0,cp);insert(s,composite,130,230);
        h=find(s,alpha,90,none);require(h!=null&&Math.abs(h.x-130)<=1,"Single-color transparent icon silhouette recognized");
        Mat big=new Mat(960,720,CvType.CV_8UC4,new Scalar(20,24,29,255));insert(big,t,301,477);try(VisionMatcher.Frame f=new VisionMatcher.Frame(big,480,100,none)){h=VisionMatcher.find(f,t,null,System.nanoTime()+4000000000L);require(h!=null&&h.x==301&&h.y==477,"Coarse location refines exact full-resolution coordinates");}
        Mat trackingScreen=screen();insert(trackingScreen,t,71,143);VisionMatcher.Hit prior=find(trackingScreen,t,94,none);trackingScreen=screen();insert(trackingScreen,t,78,150);try(VisionMatcher.Frame f=new VisionMatcher.Frame(trackingScreen,720,94,none)){h=VisionMatcher.find(f,t,prior,System.nanoTime()+4000000000L);require(h!=null&&Math.abs(h.x-78)<=1,"Tracking finds a nearby moving target");}
        trackingScreen=screen();insert(trackingScreen,t,170,300);try(VisionMatcher.Frame f=new VisionMatcher.Frame(trackingScreen,720,94,none)){h=VisionMatcher.find(f,t,prior,System.nanoTime()+4000000000L);require(h!=null&&Math.abs(h.x-170)<=1,"Tracking falls back to the full screen after a large move");}
        // Every frame is searched, even when every pixel is unchanged.
        for(int i=0;i<4;i++){s=screen();insert(s,t,71,143);h=find(s,t,96,none);require(h!=null&&h.x==71,"Still frames must continue to match");}
        Mat cross=new Mat(24,24,CvType.CV_8UC4,new Scalar(0,0,0,0));Imgproc.line(cross,new Point(4,4),new Point(19,19),new Scalar(245,245,245,255),3);Imgproc.line(cross,new Point(19,4),new Point(4,19),new Scalar(245,245,245,255),3);
        byte[] xp=new byte[24*24*4];cross.get(0,0,xp);
        for(int scene=0;scene<3;scene++){
            s=screen();byte[] pixels=new byte[240*360*4];s.get(0,0,pixels);Random rng=new Random(42+scene);
            for(int i=0;i<pixels.length;i+=4){int v=scene==0?40:rng.nextInt(180);pixels[i]=(byte)v;pixels[i+1]=(byte)(v/2);pixels[i+2]=(byte)(v/3);pixels[i+3]=(byte)255;}
            for(int y=0;y<24;y++)for(int x=0;x<24;x++){int a4=(y*24+x)*4,b4=((y+180)*240+x+100)*4;if((xp[a4+3]&255)>=128)for(int c=0;c<4;c++)pixels[b4+c]=xp[a4+c];}
            s.put(0,0,pixels);Mat exactScene=s.clone();h=find(s,cross,90,none);System.out.println("Scene "+scene+": "+(h==null?"none":h.x+","+h.y+" "+h.width+"x"+h.height+" score "+h.score));require(h!=null&&Math.abs(h.x-100)<=1&&Math.abs(h.y-180)<=1,"Masked cross must ignore changing scenery, scene "+scene);VisionMatcher.Hit exactHit=find(exactScene,cross,100,none);require(exactHit!=null&&exactHit.x==100&&exactHit.y==180,"100% compares retained pixels only");
        }
        s=new Mat(360,240,CvType.CV_8UC4,new Scalar(245,245,245,255));require(find(s,cross,90,none)==null,"Flat white is not a white cross");
        s=screen();Imgproc.line(s,new Point(112,184),new Point(112,199),new Scalar(245,245,245,255),3);Imgproc.line(s,new Point(104,192),new Point(119,192),new Scalar(245,245,245,255),3);require(find(s,cross,90,none)==null,"A plus is not a cross");
        Mat tiny=new Mat(8,8,CvType.CV_8UC4,new Scalar(10,20,30,255));Imgproc.line(tiny,new Point(1,1),new Point(6,6),new Scalar(250,230,210,255),1);Imgproc.line(tiny,new Point(6,1),new Point(1,6),new Scalar(250,230,210,255),1);s=screen();insert(s,tiny,91,113);h=find(s,tiny,100,none);require(h!=null&&h.x==91&&h.y==113,"8x8 distinctive target at native resolution");tiny.release();cross.release();

        Mat doubled=new Mat();Imgproc.resize(t,doubled,new Size(t.cols()*2,t.rows()*2),0,0,Imgproc.INTER_LINEAR);
        Mat larger=new Mat(600,480,CvType.CV_8UC4,new Scalar(20,24,29,255));insert(larger,doubled,300,90);
        long started=System.nanoTime();try(VisionMatcher.Frame f=new VisionMatcher.Frame(larger,720,96,none)){h=VisionMatcher.find(f,t,null,System.nanoTime()+4000000000L,2,new RegionRules.Region(.5f,0,1,.5f,75));require(h!=null&&h.x==300&&h.y==90,"Source-width calibration must find a doubled icon");}System.out.println("Calibrated regional 2x match ms="+(System.nanoTime()-started)/1000000);
        s=screen();insert(s,t,20,30);try(VisionMatcher.Frame f=new VisionMatcher.Frame(s,720,90,none)){require(VisionMatcher.find(f,t,null,System.nanoTime()+4000000000L,1,new RegionRules.Region(.6f,0,1,.5f,75))==null,"Identical symbol outside per-target region must not trigger");}
        s=screen();insert(s,t,20,30);insert(s,t,170,30);try(VisionMatcher.Frame f=new VisionMatcher.Frame(s,720,100,none)){h=VisionMatcher.find(f,t,null,System.nanoTime()+4000000000L,1,new RegionRules.Region(.6f,0,1,.5f,75));require(h!=null&&h.x==170,"Allowed copy selected, out-of-zone copy ignored at 100%");}
        s=screen();insert(s,t,170,30);try(VisionMatcher.Frame f=new VisionMatcher.Frame(s,720,90,Arrays.asList(new RegionRules.Region(.6f,0,1,.5f,90,true)))){require(VisionMatcher.find(f,t,null,System.nanoTime()+4000000000L,1,new RegionRules.Region(.6f,0,1,.5f,75))==null,"Global exclusion must still override per-target search area");}doubled.release();
        t.release();bright.release();scaled.release();wrong.release();alpha.release();composite.release();System.out.println("PASS native vision: exact, brightness, scale, background and text distractors, excluded overlap, regional thresholds, alpha icons, full-resolution refinement, still screens, masked changing backgrounds, 8x8 target, masked exact mode");
    }
}
