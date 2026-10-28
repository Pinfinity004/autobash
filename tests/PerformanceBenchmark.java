package com.pratik.imagetap;
import org.opencv.core.*;import java.util.*;
/** Synthetic desktop comparison; not a prediction of mobile performance. Run without other builds. */
public class PerformanceBenchmark {
 public static void main(String[] args){nu.pattern.OpenCV.loadLocally();Mat screen=new Mat(1280,720,CvType.CV_8UC4,new Scalar(15,25,35,255));List<Mat> templates=new ArrayList<>();List<ScanEngine.Job> jobs=new ArrayList<>();for(int i=0;i<51;i++){final Mat t=ParallelScanTest.template(i);templates.add(t);int x=20+i%9*75,y=40+i/9*100;Mat patch=screen.submat(y,y+32,x,x+48);t.copyTo(patch);patch.release();jobs.add(new ScanEngine.Job("image-"+i,1,null,()->t.clone(),null));}
 ScanEngine[] engines={new ScanEngine(1),new ScanEngine(2),new ScanEngine(4)};long[][] times=new long[3][7];ScanEngine.Result r=null;
 try{for(int warm=0;warm<4;warm++)for(int mode=0;mode<3;mode++){Core.setNumThreads(mode==0?2:1);r=engines[mode].scan(screen,jobs,96,Collections.emptyList(),true,false,()->false,n->{});}
 for(int round=0;round<7;round++)for(int turn=0;turn<3;turn++){int mode=(turn+round)%3;Core.setNumThreads(mode==0?2:1);times[mode][round]=engines[mode].scan(screen,jobs,96,Collections.emptyList(),true,false,()->false,n->{}).millis;}
 for(int mode=0;mode<3;mode++){Arrays.sort(times[mode]);System.out.println(new String[]{"Serial, 2 OpenCV threads","Balanced, 2 workers","Fast, 4 workers"}[mode]+": median "+times[mode][3]+" ms; range "+times[mode][0]+"–"+times[mode][6]+" ms");}
 List<ScanEngine.Job> recent=new ArrayList<>();for(int i=0;i<51;i++){ScanEngine.Job j=jobs.get(i);recent.add(new ScanEngine.Job(j.key,j.scale,j.area,j.loader,r.hits[i]));}Core.setNumThreads(1);long[] tracked=new long[7];for(int i=0;i<7;i++)tracked[i]=engines[1].scan(screen,recent,96,Collections.emptyList(),true,false,()->false,n->{}).millis;Arrays.sort(tracked);System.out.println("Balanced, recent exact locations: median "+tracked[3]+" ms");
 Mat single=new Mat(1280,720,CvType.CV_8UC4,new Scalar(15,25,35,255));Mat patch=single.submat(400,432,300,348);templates.get(25).copyTo(patch);patch.release();
 try{for(int round=0;round<7;round++)for(int turn=0;turn<3;turn++){int mode=(turn+round)%3;Core.setNumThreads(mode==0?2:1);times[mode][round]=engines[mode].scan(single,jobs,96,Collections.emptyList(),true,false,()->false,n->{}).millis;}for(int mode=0;mode<3;mode++){Arrays.sort(times[mode]);System.out.println("One visible target, "+new String[]{"Serial","Balanced","Fast"}[mode]+": median "+times[mode][3]+" ms");}}finally{single.release();}
 }finally{for(ScanEngine e:engines)e.close();screen.release();for(Mat t:templates)t.release();}
 }
}
