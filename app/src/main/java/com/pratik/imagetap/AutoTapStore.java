package com.pratik.imagetap;
import android.content.*;
final class AutoTapStore {
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("auto-tap",0);}
 static AutoTapPlan load(Context c){String raw=prefs(c).getString("plan",null);return raw==null?new AutoTapPlan():AutoTapPlan.decode(raw);}
 static void save(Context c,AutoTapPlan p){if(!prefs(c).edit().putString("plan",p.encode()).commit())throw new IllegalStateException("Could not save Auto Tap layout");}
 static void slot(Context c,int slot,String name,AutoTapPlan p){if(slot<0||slot>2||name.trim().isEmpty()||name.length()>60)throw new IllegalArgumentException("Enter a layout name (1–60 characters)");if(!prefs(c).edit().putString("slot"+slot,p.encode()).putString("name"+slot,name.trim()).commit())throw new IllegalStateException("Could not save layout");}
 static AutoTapPlan slot(Context c,int slot){String raw=prefs(c).getString("slot"+slot,null);if(raw==null)throw new IllegalArgumentException("This slot is empty");return AutoTapPlan.decode(raw);}
 static void progress(Context c,AutoTapRunner r,boolean durable){SharedPreferences.Editor e=prefs(c).edit().putLong("taps",r.taps).putLong("cyclesDone",r.cycles).putInt("next",r.index).putLong("remaining",r.remaining()).putBoolean("done",r.done).putBoolean("pending",r.pending);if(durable)e.commit();else e.apply();}
 static void restore(Context c,AutoTapRunner r){SharedPreferences p=prefs(c);r.taps=Math.max(0,p.getLong("taps",0));r.cycles=Math.max(0,p.getLong("cyclesDone",0));r.index=Math.max(0,Math.min(r.plan.points.size()-1,p.getInt("next",0)));r.remaining=Math.max(0,Math.min(60000,p.getLong("remaining",r.plan.startDelay)));r.done=p.getBoolean("done",false);r.message=p.getBoolean("pending",false)?"Recovered paused · last tap outcome unknown":"Recovered paused · check positions, then Play";}
}
