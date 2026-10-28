package com.pratik.imagetap;
/** Fair bounded batches; a pending detection is rechecked in the next frame. */
final class TargetSchedule {
    private int cursor,priorityStreak,deferred=-1,retries;
    void defer(int id){cursor=id;}
    void timedOut(int id){if(deferred==id)retries++;else{deferred=id;retries=1;}if(retries<=2)cursor=id;else{cursor=id+1;deferred=-1;retries=0;}}
    void completed(int id){if(deferred==id){deferred=-1;retries=0;}}
    int[] next(int count,int priority){return next(count,priority,4);}
    int[] next(int count,int priority,int maximum){
        if(count<=0)return new int[0];
        int[] batch=new int[Math.min(Math.max(1,maximum),count)];int used=0;
        boolean usePriority=priority>=0&&priority<count&&priorityStreak<2;
        if(usePriority){batch[used++]=priority;priorityStreak++;}else priorityStreak=0;
        while(used<batch.length){int id=cursor%count;cursor=(cursor+1)%count;if(!usePriority||id!=priority)batch[used++]=id;}
        return batch;
    }
}
