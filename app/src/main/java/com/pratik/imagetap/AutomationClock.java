package com.pratik.imagetap;
import java.util.*;
/** Single action lane; intervals begin on completion, timeline delays follow completion. */
final class AutomationClock {
 final List<TimedAction> actions;final boolean timeline,loop;final long[] due;int index,pending=-1;long pausedAt=-1,startedAt;boolean done;
 AutomationClock(List<TimedAction> actions,boolean timeline,boolean loop,long now){this.actions=new ArrayList<>(actions);this.timeline=timeline;this.loop=loop;due=new long[actions.size()];for(int i=0;i<due.length;i++)due[i]=now+actions.get(i).delay;done=actions.isEmpty();}
 void pause(long now){if(pausedAt<0)pausedAt=now;}
 void resume(long now){if(pausedAt>=0){long shift=Math.max(0,now-pausedAt);for(int i=0;i<due.length;i++)due[i]+=shift;pausedAt=-1;}}
 int next(){if(done||pending>=0||pausedAt>=0)return -1;if(timeline)return index;int best=0;for(int i=1;i<due.length;i++)if(due[i]<due[best])best=i;return best;}
 long remaining(long now){int n=next();return n<0?0:Math.max(0,due[n]-now);}
 int take(long now){int n=next();if(n<0||now<due[n])return -1;pending=n;startedAt=now;return n;}
 void complete(long now){if(pending<0)return;long base=pausedAt>=0?pausedAt:now;int finished=pending;pending=-1;if(timeline){index=finished+1;if(index==actions.size()){if(loop)index=0;else{done=true;return;}}due[index]=base+actions.get(index).delay;}else due[finished]=base+actions.get(finished).delay;}
 void failed(long now){if(pending<0)return;due[pending]=(pausedAt>=0?pausedAt:now)+500;pending=-1;}
}
