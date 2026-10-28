package com.pratik.imagetap;
import org.opencv.core.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/** One coordinator; lane-local Mats and caches. No mutable Frame crosses threads. */
final class ScanEngine implements AutoCloseable {
 static final class Job {
  final String key;final double scale;final RegionRules.Region area;final Supplier<Mat> loader;final VisionMatcher.Hit previous;
  Job(String key,double scale,RegionRules.Region area,Supplier<Mat> loader,VisionMatcher.Hit previous){this.key=key;this.scale=scale;this.area=area;this.loader=loader;this.previous=previous;}
  String group(){return key+":"+scale+":"+(area==null?"all":area.left+","+area.top+","+area.right+","+area.bottom);}
 }
 static final class Result {
  final VisionMatcher.Hit[] hits;final boolean[] checked;final int unique,workers;final long millis;final String diagnostic;
  Result(VisionMatcher.Hit[] hits,boolean[] checked,int unique,int workers,long millis,String diagnostic){this.hits=hits;this.checked=checked;this.unique=unique;this.workers=workers;this.millis=millis;this.diagnostic=diagnostic;}
 }
 static final class Lane implements AutoCloseable {
  final ExecutorService executor=Executors.newSingleThreadExecutor();final VisionMatcher.Cache cache=new VisionMatcher.Cache();
  final LinkedHashMap<String,Mat> images=new LinkedHashMap<>(32,.75f,true);long bytes;
  Mat image(Job job){Mat m=images.get(job.key);if(m!=null)return m;m=job.loader.get();if(m==null)return null;images.put(job.key,m);bytes+=m.total()*m.elemSize();while(images.size()>500||bytes>24L*1024*1024){String key=images.keySet().iterator().next();if(key.equals(job.key))break;Mat old=images.remove(key);cache.clear();bytes-=old.total()*old.elemSize();old.release();}return m;}
  public void close(){executor.shutdown();cache.close();for(Mat m:images.values())m.release();images.clear();}
 }
 private final Lane[] lanes;
 private long scanTurn;
 ScanEngine(int count){lanes=new Lane[Math.max(1,Math.min(4,count))];for(int i=0;i<lanes.length;i++)lanes[i]=new Lane();}
 int workers(){return lanes.length;}
 Result scan(Mat original,List<Job> jobs,int accuracy,List<RegionRules.Region> regions,boolean hybrid,boolean alternate,BooleanSupplier cancelled,IntConsumer progress){
  long started=System.nanoTime();VisionMatcher.Hit[] hits=new VisionMatcher.Hit[jobs.size()];boolean[] checked=new boolean[jobs.size()];
  Map<String,List<Integer>> groups=new TreeMap<>();for(int i=0;i<jobs.size();i++)groups.computeIfAbsent(jobs.get(i).group(),k->new ArrayList<>()).add(i);
  List<List<List<Integer>>> assignments=new ArrayList<>();for(int i=0;i<lanes.length;i++)assignments.add(new ArrayList<>());
  for(Map.Entry<String,List<Integer>> e:groups.entrySet())assignments.get(Math.floorMod(e.getKey().hashCode(),lanes.length)).add(e.getValue());
  // Stable content order preserves cache ownership and decouples scan priority from the grid.
  // Interleave distant entries, then rotate the starting position every pulse (including cancelled scans).
  long turn=scanTurn++;for(List<List<Integer>> batch:assignments){List<List<Integer>> ordered=new ArrayList<>(batch);int size=batch.size();if(size<2)continue;int stride=size/2+1;while(gcd(stride,size)!=1)stride++;int offset=(int)Math.floorMod(turn,size);for(int i=0;i<size;i++)batch.set(i,ordered.get((offset+i*stride)%size));}
  java.util.concurrent.atomic.AtomicInteger done=new java.util.concurrent.atomic.AtomicInteger();List<Future<String>> tasks=new ArrayList<>();
  for(int n=0;n<lanes.length;n++){final Lane lane=lanes[n];final List<List<Integer>> batch=assignments.get(n);if(batch.isEmpty())continue;
   tasks.add(lane.executor.submit(()->{
    try(VisionMatcher.Frame frame=new VisionMatcher.Frame(new Mat(original,new Rect(0,0,original.cols(),original.rows())),720,accuracy,regions,lane.cache)){
     frame.hybrid=hybrid;frame.cancelled=cancelled;VisionMatcher.Frame fine=null;
     try{for(List<Integer> ids:batch){if(cancelled.getAsBoolean())break;Job job=jobs.get(ids.get(0));Mat template=lane.image(job);VisionMatcher.Hit hit=null;
      if(template!=null){VisionMatcher.Frame chosen=frame;if(Math.min(template.cols(),template.rows())*job.scale*frame.factor<24){if(fine==null){fine=new VisionMatcher.Frame(new Mat(original,new Rect(0,0,original.cols(),original.rows())),original.cols(),accuracy,regions,lane.cache);fine.hybrid=hybrid;fine.cancelled=cancelled;}chosen=fine;}
       hit=VisionMatcher.findExpected(chosen,template,job.previous,job.scale,job.area);
       if(alternate&&(hit==null||hit.score<100)){VisionMatcher.Hit scaled=VisionMatcher.find(chosen,template,null,Long.MAX_VALUE,job.scale,job.area);if(scaled!=null&&(hit==null||scaled.score>hit.score))hit=scaled;}
       if(chosen!=frame)frame.observe(chosen.closest,chosen.closestRequired,chosen.strictMismatch);
      }
      for(int id:ids){hits[id]=hit;checked[id]=true;}progress.accept(done.addAndGet(ids.size()));
     }return frame.diagnostic();}finally{if(fine!=null)fine.close();}
    }
   }));
  }
  RuntimeException failure=null;String diagnostic="No close match";
  // Always join every native worker before releasing the input or its caches, even on failure.
  boolean interrupted=false;
  for(Future<String> f:tasks){for(;;)try{String d=f.get();if(!d.equals("No close match"))diagnostic=d;break;}catch(InterruptedException e){interrupted=true;}catch(ExecutionException e){failure=new IllegalStateException("Recognition worker failed",e.getCause());break;}}
  if(interrupted)Thread.currentThread().interrupt();if(failure!=null)throw failure;
  return new Result(hits,checked,groups.size(),tasks.size(),(System.nanoTime()-started)/1000000,diagnostic);
 }
 static int gcd(int a,int b){while(b!=0){int next=a%b;a=b;b=next;}return a;}
 public void close(){for(Lane lane:lanes)lane.close();}
}
