package com.pratik.imagetap;
/** Single gesture lane. No timer floor, overlapping dispatch, recursion, or catch-up bursts. */
final class AutoTapRunner {
 interface Scheduler {long now();void post(Runnable r,long delay);void cancel(Runnable r);}
 interface Driver {boolean send(int target,int hold,Result result);}
 interface Result {void finish(boolean completed);}
 interface Guard {String blocked();}
 final AutoTapPlan plan;final Scheduler time;final Driver driver;final Guard guard;final Runnable changed;
 boolean playing,pending,closed,done;long taps,cycles,due,remaining;int index;String message="Drag targets, then Play";private long ticket;private Runnable task,watchdog;
 AutoTapRunner(AutoTapPlan p,Scheduler t,Driver d,Guard g,Runnable listener){p.validate();plan=p;time=t;driver=d;guard=g;changed=listener;remaining=p.startDelay;}
 void play(){if(closed||pending||playing)return;if(done){taps=cycles=0;index=0;done=false;remaining=plan.startDelay;}String block=guard.blocked();if(block!=null){message=block;changed.run();return;}playing=true;message="Running";queue(remaining);changed.run();}
 void pause(String reason){if(closed)return;if(playing&&!pending)remaining=Math.max(0,due-time.now());playing=false;cancelTask();message=reason;changed.run();}
 void stop(){if(closed)return;playing=false;closed=true;ticket++;cancelTask();if(watchdog!=null)time.cancel(watchdog);pending=false;message="Stopped";changed.run();}
 long remaining(){return playing&&!pending?Math.max(0,due-time.now()):remaining;}
 private void cancelTask(){if(task!=null)time.cancel(task);task=null;}
 private void queue(long wait){cancelTask();due=time.now()+Math.max(0,wait);task=this::send;time.post(task,Math.max(0,wait));}
 private void send(){task=null;if(!playing||closed||pending)return;String block=guard.blocked();if(block!=null){pause(block);return;}pending=true;final long id=++ticket;final int target=index;final int hold=plan.hold(target);watchdog=()->{if(!closed&&pending&&id==ticket){ticket++;pending=false;remaining=0;pause("No completion received · check screen before Play");}};time.post(watchdog,hold+2000);
  Result result=ok->{if(closed||!pending||id!=ticket)return;time.cancel(watchdog);watchdog=null;pending=false;if(!ok){remaining=0;pause("Tap cancelled or rejected · paused");return;}taps++;index++;if(index==plan.points.size()){index=0;cycles++;}remaining=plan.gap(target);if(plan.cycles>0&&cycles>=plan.cycles){done=true;playing=false;message="Cycle limit reached";}if(playing)queue(remaining);changed.run();};
  try{if(!driver.send(target,hold,result))result.finish(false);}catch(RuntimeException e){result.finish(false);}
 }
}
