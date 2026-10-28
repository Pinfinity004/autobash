package com.pratik.imagetap;
public class TargetScheduleTest {
    static void require(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args){
        TargetSchedule s=new TargetSchedule();boolean[] seen=new boolean[100];
        for(int j=0;j<25;j++){int[] batch=s.next(100,-1);require(batch.length==4,"bounded batch");for(int id:batch)seen[id]=true;}
        for(boolean b:seen)require(b,"all 100 targets scanned");
        seen=new boolean[100];for(int j=0;j<35;j++){int[] batch=s.next(100,77);boolean[] unique=new boolean[100];for(int id:batch){require(!unique[id],"no duplicate per batch");unique[id]=true;seen[id]=true;}}
        for(boolean b:seen)require(b,"priority does not starve remaining targets");
        seen=new boolean[100];for(int i=0;i<310;i++)for(int id:s.next(100,77,1))seen[id]=true;
        for(boolean b:seen)require(b,"single-target adaptive batches must not starve the remaining targets");
        TargetSchedule large=new TargetSchedule();boolean[] all=new boolean[500];
        for(int i=0;i<125;i++)for(int id:large.next(500,-1))all[id]=true;
        for(boolean hit:all)require(hit,"500 target coverage");
        large=new TargetSchedule();int[] first=large.next(500,-1);large.defer(first[1]);require(large.next(500,-1)[0]==1,"unfinished batch resumes at first unchecked target");
        large.timedOut(1);large.next(500,-1);large.timedOut(1);large.next(500,-1);large.timedOut(1);require(large.next(500,-1)[0]==2,"permanently slow target cannot starve the library");
        require(s.next(1,0).length==1,"single target");require(s.next(0,-1).length==0,"empty target list");
        System.out.println("PASS: 500-target coverage, confirmation priority, fairness, unique batches, empty/single target");
    }
}
